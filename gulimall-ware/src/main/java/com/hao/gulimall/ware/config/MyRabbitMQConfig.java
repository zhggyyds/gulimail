package com.hao.gulimall.ware.config;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.ware.config
 * @Description:
 * @date 2022/12/7 22:18
 **/
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * 创建队列，交换机，延时队列，绑定关系 的configuration
 * 1.Broker中的Queue、Exchange、Binding不存在的情况下，会自动创建（在RabbitMQ），不会重复创建覆盖
 * 2.懒加载，只有第一次使用的时候才会创建（例如监听队列）
 */
@Configuration
public class MyRabbitMQConfig {

    @Bean
    public MessageConverter messageConverter() {
        // 使用json序列化器来序列化消息，发送消息时，消息对象会被序列化成json格式
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 交换机
     * Topic，可以绑定多个队列
     */
    @Bean
    public Exchange stockEventExchange() {
        //String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 死信队列
     * 释放库存
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 延时队列
     * 锁定库存
     */
    @Bean
    public Queue stockDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        // 消息过期时间 1.5分钟
        arguments.put("x-message-ttl", 90000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    /**
     * 绑定：交换机与死信队列
     * 释放库存
     */
    @Bean
    public Binding stockReleasedBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			Map<String, Object> arguments
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }

    /**
     * 绑定：交换机与延时队列
     * 锁定库存
     */
    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }
}
