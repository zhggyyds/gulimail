package com.hao.cart.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth.config
 * @Description:
 * @date 2022/11/15 20:47
 **/

@Configuration
public class MySessionConfig {
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setDomainName("gulimall.com");// 放大作用域
        cookieSerializer.setCookieName("GULISESSION");
        cookieSerializer.setCookieMaxAge(60 * 60 * 24 * 7);// 指定cookie有效期7天，会话级关闭浏览器后cookie即失效
        return cookieSerializer;
    }

    // 指定session序列化到redis的序列化器
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
//        return new GenericFastJsonRedisSerializer();
    }
}
