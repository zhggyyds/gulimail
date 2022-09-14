package com.hao.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;



//跨域配置文件
// 一个网站中访问不同域的文件需要配置如下的信息来允许跨域访问
// 由于 是从 8001端口访问 88 端口下的文件（88端口是网关，用于跳转，但是不设置跨域就不能实现跨域）
// 因为跨域配置配置在被进行跨域请求的web程序下
@Configuration // gateway
public class GulimallCorsConfiguration {

    @Bean // 添加过滤器
    public CorsWebFilter corsWebFilter(){
        // 基于url跨域，选择reactive包下的
        // spring中帮我们实现好的用于设置跨域的
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();
        // 跨域配置信息
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        // 允许跨域的头
        corsConfiguration.addAllowedHeader("*");
        // 允许跨域的请求方式
        corsConfiguration.addAllowedMethod("*");
        // 允许跨域的请求来源
        corsConfiguration.addAllowedOrigin("*");
        // 是否允许携带cookie跨域
        corsConfiguration.setAllowCredentials(true);

        // 任意url都要进行跨域配置
        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(source);
    }
}