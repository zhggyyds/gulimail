package com.hao.cart;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.gateway
 * @Description:
 * @date 2022/8/7 23:33
 **/

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableDiscoveryClient
@EnableRedisHttpSession
@EnableFeignClients
public class GulimallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallCartApplication.class, args);

    }
}
