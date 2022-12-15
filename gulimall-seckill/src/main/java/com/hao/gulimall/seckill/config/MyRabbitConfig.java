package com.hao.gulimall.seckill.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * 创建队列，交换机，延时队列，绑定关系 的configuration
 * 1.Broker中的Queue、Exchange、Binding不存在的情况下，会自动创建（在RabbitMQ），不会重复创建覆盖
 * 2.懒加载，只有第一次使用的时候才会创建（例如监听队列）
 */
@Configuration
public class MyRabbitConfig {

    @Bean
    public MessageConverter messageConverter() {
        // 使用json序列化器来序列化消息，发送消息时，消息对象会被序列化成json格式
        return new Jackson2JsonMessageConverter();
    }

}
