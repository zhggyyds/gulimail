package com.hao.gulimall.seckill;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.seckill
 * @Description:
 * @date 2022/12/11 14:03
 **/

@EnableRabbit
@EnableRedisHttpSession
@EnableScheduling
// 服务注册发现
@EnableFeignClients
@EnableDiscoveryClient
// springboot启动类 并排除数据库信息
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GulimallSeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallSeckillApplication.class, args);
    }
}
