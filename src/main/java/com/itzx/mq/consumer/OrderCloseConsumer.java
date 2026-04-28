package com.itzx.mq.consumer;

import com.itzx.electronics.mapper.ProductMapper;
import com.itzx.mq.RabbitMqConstants;
import com.itzx.mq.dto.OrderCloseMessage;
import com.itzx.order.mapper.OrdersMapper;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderCloseConsumer {

    private static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String SECKILL_BUY_KEY_PREFIX = "seckill:buy:";

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @RabbitListener(queues = RabbitMqConstants.ORDER_CLOSE_QUEUE)
    @Transactional
    public void onMessage(OrderCloseMessage msg) {
        if (msg == null || msg.getOrderNo() == null || msg.getOrderNo().isBlank()) {
            return;
        }

        // 仅当订单仍为待支付时才关闭
        int affected = ordersMapper.closeOrderIfUnpaid(msg.getOrderNo());
        if (affected <= 0) {
            return;
        }

        // 回补数据库库存
        productMapper.increaseStock(msg.getProductId(), 1);

        // 回补 Redis 秒杀库存，并清理限购 key（允许再次购买/重新抢购）
        try {
            stringRedisTemplate.opsForValue().increment(SECKILL_STOCK_KEY_PREFIX + msg.getProductId());
            stringRedisTemplate.delete(SECKILL_BUY_KEY_PREFIX + msg.getProductId() + ":" + msg.getUserId());
        } catch (Exception ignored) {
        }
    }
}
