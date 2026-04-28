package com.itzx.mq;

public final class RabbitMqConstants {
    private RabbitMqConstants() {
    }

    public static final String SECKILL_EXCHANGE = "seckill.order.exchange";
    public static final String SECKILL_QUEUE = "seckill.order.queue";
    public static final String SECKILL_ROUTING_KEY = "seckill.order.create";

    public static final String ORDER_CLOSE_EXCHANGE = "order.close.exchange";
    public static final String ORDER_CLOSE_QUEUE = "order.close.queue";
    public static final String ORDER_CLOSE_ROUTING_KEY = "order.close";

    public static final String ORDER_CLOSE_DELAY_EXCHANGE = "order.close.delay.exchange";
    public static final String ORDER_CLOSE_DELAY_QUEUE = "order.close.delay.queue";
    public static final String ORDER_CLOSE_DELAY_ROUTING_KEY = "order.close.delay";

    public static final long ORDER_CLOSE_DELAY_MILLIS = 30L * 60L * 1000L;
}
