package com.itzx.mq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    @Bean
    // 秒杀交换机
    public DirectExchange seckillExchange() {
        return new DirectExchange(RabbitMqConstants.SECKILL_EXCHANGE, true, false);
    }

    @Bean
    // 秒杀队列
    public Queue seckillQueue() {
        return QueueBuilder.durable(RabbitMqConstants.SECKILL_QUEUE).build();
    }

    @Bean
    // 秒杀队列绑定交换机
    public Binding seckillBinding(DirectExchange seckillExchange, Queue seckillQueue) {
        return BindingBuilder.bind(seckillQueue)
                .to(seckillExchange)
                .with(RabbitMqConstants.SECKILL_ROUTING_KEY);
    }

    @Bean
    // 订单关闭交换机
    public DirectExchange orderCloseExchange() {
        return new DirectExchange(RabbitMqConstants.ORDER_CLOSE_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCloseQueue() {
        return QueueBuilder.durable(RabbitMqConstants.ORDER_CLOSE_QUEUE).build();
    }

    @Bean
    public Binding orderCloseBinding(DirectExchange orderCloseExchange, Queue orderCloseQueue) {
        return BindingBuilder.bind(orderCloseQueue)
                .to(orderCloseExchange)
                .with(RabbitMqConstants.ORDER_CLOSE_ROUTING_KEY);
    }

    @Bean
    public DirectExchange orderCloseDelayExchange() {
        return new DirectExchange(RabbitMqConstants.ORDER_CLOSE_DELAY_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderCloseDelayQueue() {
        return QueueBuilder.durable(RabbitMqConstants.ORDER_CLOSE_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.ORDER_CLOSE_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.ORDER_CLOSE_ROUTING_KEY)
                .withArgument("x-message-ttl", RabbitMqConstants.ORDER_CLOSE_DELAY_MILLIS)
                .build();
    }

    @Bean
    public Binding orderCloseDelayBinding(DirectExchange orderCloseDelayExchange, Queue orderCloseDelayQueue) {
        return BindingBuilder.bind(orderCloseDelayQueue)
                .to(orderCloseDelayExchange)
                .with(RabbitMqConstants.ORDER_CLOSE_DELAY_ROUTING_KEY);
    }
}
