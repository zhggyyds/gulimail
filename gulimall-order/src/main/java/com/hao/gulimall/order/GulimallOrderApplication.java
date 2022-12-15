package com.hao.gulimall.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

// 开启动态代理，使用aspectj代替jdk作动态代理
@EnableAspectJAutoProxy(exposeProxy = true)

@MapperScan("com.hao.gulimall.order.dao")
@SpringBootApplication
@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@EnableRedisHttpSession
public class GulimallOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallOrderApplication.class, args);

    }

}
