package com.itzx.seckill.biz.imp;

import com.itzx.electronics.entity.Product;
import com.itzx.electronics.mapper.ProductMapper;
import com.itzx.order.entity.OrderItem;
import com.itzx.order.entity.Orders;
import com.itzx.order.enums.OrderStatus;
import com.itzx.order.mapper.OrderItemMapper;
import com.itzx.order.mapper.OrdersMapper;
import com.itzx.seckill.biz.SeckillBiz;
import com.itzx.until.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class SeckillBizImpl implements SeckillBiz {

    private static final String SECKILL_STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String SECKILL_BUY_KEY_PREFIX = "seckill:buy:";

    private static final long BUY_RECORD_TTL_SECONDS = 24 * 60 * 60;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setResultType(Long.class);
        SECKILL_SCRIPT.setScriptText(
                "local stockKey = KEYS[1] " +
                        "local buyKey = KEYS[2] " +
                        "local ttl = tonumber(ARGV[1]) " +
                        "if redis.call('exists', buyKey) == 1 then return 2 end " +
                        "local stock = tonumber(redis.call('get', stockKey) or '-1') " +
                        "if stock <= 0 then return 0 end " +
                        "redis.call('decr', stockKey) " +
                        "redis.call('set', buyKey, '1') " +
                        "redis.call('expire', buyKey, ttl) " +
                        "return 1"
        );
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public Result preloadStock(int productId, int stock) {
        if (productId <= 0) {
            return Result.error("商品ID不能为空");
        }
        if (stock < 0) {
            return Result.error("库存不能小于0");
        }
        String stockKey = buildStockKey(productId);
        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(stock));
        return Result.success("预热成功");
    }

    @Override
    @Transactional
    public Result seckillBuy(int userId,
                            int productId,
                            String receiverName,
                            String receiverPhone,
                            String receiverAddress,
                            String remark) {
        if (userId <= 0) {
            return Result.unAuth("用户未登录");
        }
        if (productId <= 0) {
            return Result.error("商品ID不能为空");
        }

        String stockKey = buildStockKey(productId);
        String buyKey = buildBuyKey(productId, userId);

        Long scriptResult = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Arrays.asList(stockKey, buyKey),
                String.valueOf(BUY_RECORD_TTL_SECONDS)
        );

        if (scriptResult == null) {
            return Result.error("秒杀失败，请稍后重试");
        }
        if (scriptResult == 0L) {
            return Result.error("库存不足");
        }
        if (scriptResult == 2L) {
            return Result.error("已抢购过该商品");
        }

        Product product = productMapper.findProductById(productId);
        if (product == null || product.getPrice() == null) {
            rollbackRedis(stockKey, buyKey);
            return Result.error("商品不存在");
        }

        int affected = productMapper.decreaseStockIfEnough(productId, 1);
        if (affected <= 0) {
            rollbackRedis(stockKey, buyKey);
            return Result.error("库存不足");
        }

        Orders order = new Orders();
        order.setOrderNo(generateOrderNo(userId));
        order.setUserId(userId);
        order.setTotalAmount(product.getPrice());
        order.setStatus(OrderStatus.WAIT_PAY.getCode());
        order.setPayType(null);
        order.setReceiverName(receiverName);
        order.setReceiverPhone(receiverPhone);
        order.setReceiverAddress(receiverAddress);
        order.setRemark(remark);

        int inserted = ordersMapper.insertOrder(order);
        if (inserted <= 0 || order.getId() == null) {
            rollbackRedis(stockKey, buyKey);
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
            rollbackRedis(stockKey, buyKey);
            throw new RuntimeException("创建订单明细失败");
        }

        return Result.success(order);
    }

    private static String buildStockKey(int productId) {
        return SECKILL_STOCK_KEY_PREFIX + productId;
    }

    private static String buildBuyKey(int productId, int userId) {
        return SECKILL_BUY_KEY_PREFIX + productId + ":" + userId;
    }

    private void rollbackRedis(String stockKey, String buyKey) {
        try {
            stringRedisTemplate.opsForValue().increment(stockKey);
            stringRedisTemplate.delete(buyKey);
        } catch (Exception ignored) {
        }
    }

    private String generateOrderNo(int userId) {
        return System.currentTimeMillis() + String.valueOf(userId) + UUID.randomUUID().toString().replace("-", "").substring(0, 6);
    }
}
