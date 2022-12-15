package com.hao.gulimall.seckill.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.config
 * @Description:
 * @date 2022/10/22 21:42
 **/

@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
@Configuration
public class MyRedissonConfig {

    /**
     * 注入客户端实例对象
     */
    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}")String port) throws IOException {
        // 1.创建配置
        Config config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);// 单节点模式
//        config.useSingleServer().setAddress("rediss://" + host + ":" + port);// 使用安全连接
//        config.useClusterServers().addNodeAddress("127.0.0.1:7004", "127.0.0.1:7001");// 集群模式
        // 2.创建redisson客户端实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
