package com.hao.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableRedisHttpSession
@EnableCaching
@EnableFeignClients(basePackages = "com.hao.gulimall.product.feign")
@EnableDiscoveryClient
@EnableTransactionManagement
@MapperScan("com.hao.gulimall.product.dao") // 告诉spring dao借口在哪
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);

    }

}
