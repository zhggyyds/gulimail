package com.hao.gulimall.ware.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

/**
 * @Author: wanzenghui
 * @Date: 2021/12/15 0:04
 *
 *  TODO 后续再完善，将rabbitmq直接封装成一个微服务，直接调用。完善发送确认机制
 *      1.消费端手动确认
 *      2.服务端的发送确认，每一个发送的消息都在数据库做好记录。定期扫描数据库将失败的消息再次发送
 */
//@Configuration
public class MyRabbitCallbackConfig {


//    @Primary
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(messageConverter());
//        initRabbitTemplate(rabbitTemplate);
//        return rabbitTemplate;
//    }
//
//    @Bean
//    public MessageConverter messageConverter() {
//        // 使用json序列化器来序列化消息，发送消息时，消息对象会被序列化成json格式
//        return new Jackson2JsonMessageConverter();
//    }

    /**
     * 定制RabbitTemplate
     * 1、服务收到消息就会回调
     * 1、spring.rabbitmq.publisher-confirms: true
     * 2、设置确认回调
     * 2、消息正确抵达队列就会进行回调
     * 1、spring.rabbitmq.publisher-returns: true
     * spring.rabbitmq.template.mandatory: true
     * 2、设置确认回调ReturnCallback
     * <p>
     * 3、消费端确认(保证每个消息都被正确消费，此时才可以broker删除这个消息)
     */
    //@PostConstruct   // (MyRabbitConfig对象创建完成以后，执行这个方法)
    public void initRabbitTemplate(RabbitTemplate rabbitTemplate) {
        /**
         * 发送消息触发confirmCallback回调
         * @param correlationData：当前消息的唯一关联数据（如果发送消息时未指定此值，则回调时返回null）
         * @param ack：消息是否成功收到（ack=true，消息抵达Broker）
         * @param cause：失败的原因
         */
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            // TODO 发送成功回掉 将数据库的消息状态为修改为成功发送
            System.out.println("发送消息触发confirmCallback回调" +
                    "\ncorrelationData ===> " + correlationData +
                    "\nack ===> " + ack + "" +
                    "\ncause ===> " + cause);
            System.out.println("=================================================");
        });

        /**
         * 消息未到达队列触发returnCallback回调
         * 只要消息没有投递给指定的队列，就触发这个失败回调
         * @param message：投递失败的消息详细信息
         * @param replyCode：回复的状态码
         * @param replyText：回复的文本内容
         * @param exchange：接收消息的交换机
         * @param routingKey：接收消息的路由键
         */
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            // TODO 发送失败回调，需要修改数据库 消息的状态为失败
            System.out.println("消息未到达队列触发returnCallback回调" +
                    "\nmessage ===> " + message +
                    "\nreplyCode ===> " + replyCode +
                    "\nreplyText ===> " + replyText +
                    "\nexchange ===> " + exchange +
                    "\nroutingKey ===> " + routingKey);
            System.out.println("==================================================");
        });
    }
}