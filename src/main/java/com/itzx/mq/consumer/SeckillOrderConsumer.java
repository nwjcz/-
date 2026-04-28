package com.itzx.mq.consumer;

import com.itzx.electronics.entity.Product;
import com.itzx.electronics.mapper.ProductMapper;
import com.itzx.mq.RabbitMqConstants;
import com.itzx.mq.dto.OrderCloseMessage;
import com.itzx.mq.dto.SeckillOrderCreateMessage;
import com.itzx.order.entity.OrderItem;
import com.itzx.order.entity.Orders;
import com.itzx.order.enums.OrderStatus;
import com.itzx.order.mapper.OrderItemMapper;
import com.itzx.order.mapper.OrdersMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class SeckillOrderConsumer {

    private static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String SECKILL_BUY_KEY_PREFIX = "seckill:buy:";
    private static final String SECKILL_IDEMPOTENT_PREFIX = "mq:seckill:order:";

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMqConstants.SECKILL_QUEUE)
    @Transactional
    public void onMessage(SeckillOrderCreateMessage msg) {
        if (msg == null || msg.getOrderNo() == null || msg.getOrderNo().isBlank()) {
            return;
        }

        // 简单幂等：同一个 orderNo 只处理一次（避免重复投递/重复消费）
        String idempotentKey = SECKILL_IDEMPOTENT_PREFIX + msg.getOrderNo();
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", Duration.ofHours(6));
        if (ok == null || !ok) {
            return;
        }

        Orders exist = ordersMapper.findByOrderNo(msg.getOrderNo());
        if (exist != null) {
            return;
        }

        Product product = productMapper.findProductById(msg.getProductId());
        if (product == null || product.getPrice() == null) {
            rollbackRedis(msg.getProductId(), msg.getUserId());
            return;
        }

        int affected = productMapper.decreaseStockIfEnough(msg.getProductId(), 1);
        if (affected <= 0) {
            rollbackRedis(msg.getProductId(), msg.getUserId());
            return;
        }

        Orders order = new Orders();
        order.setOrderNo(msg.getOrderNo());
        order.setUserId(msg.getUserId());
        order.setTotalAmount(product.getPrice());
        order.setStatus(OrderStatus.WAIT_PAY.getCode());
        order.setPayType(null);
        order.setReceiverName(msg.getReceiverName());
        order.setReceiverPhone(msg.getReceiverPhone());
        order.setReceiverAddress(msg.getReceiverAddress());
        order.setRemark(msg.getRemark());

        int inserted = ordersMapper.insertOrder(order);
        if (inserted <= 0 || order.getId() == null) {
            rollbackRedis(msg.getProductId(), msg.getUserId());
            throw new RuntimeException("创建订单失败");
        }

        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setProductId(product.getId());
        item.setProductName(product.getPname());
        item.setProductImage(product.getImageUrl());
        item.setUnitPrice(product.getPrice());
        item.setQuantity(1);
        item.setTotalPrice(product.getPrice());

        int itemInserted = orderItemMapper.insertOrderItem(item);
        if (itemInserted <= 0) {
            rollbackRedis(msg.getProductId(), msg.getUserId());
            throw new RuntimeException("创建订单明细失败");
        }

        // 发送 30 分钟后关闭订单的延迟消息（TTL + DLX）
        OrderCloseMessage closeMessage = new OrderCloseMessage(order.getOrderNo(), msg.getProductId(), msg.getUserId());
        rabbitTemplate.convertAndSend(
                RabbitMqConstants.ORDER_CLOSE_DELAY_EXCHANGE,
                RabbitMqConstants.ORDER_CLOSE_DELAY_ROUTING_KEY,
                closeMessage
        );
    }

    private void rollbackRedis(int productId, int userId) {
        try {
            stringRedisTemplate.opsForValue().increment(SECKILL_STOCK_KEY_PREFIX + productId);
            stringRedisTemplate.delete(SECKILL_BUY_KEY_PREFIX + productId + ":" + userId);
        } catch (Exception ignored) {
        }
    }
}
