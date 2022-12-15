package com.hao.gulimall.order.config;

import com.hao.gulimall.order.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhouhao
 * @PackageName:com.hao.cart.config
 * @Description: 配置拦截器
 * @date 2022/11/23 16:11
 **/

@Configuration
public class MyInterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private LoginUserInterceptor loginUserInterceptor;

    /**
     * 配置拦截器生效
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");// 访问任何订单请求需要拦截校验登录
    }
}
