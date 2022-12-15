package com.hao.gulimall.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth.config
 * @Description: TODO 设置url和视图的映射，如果写视图控制器只是为了映射视图
 * @date 2022/11/11 13:30
 **/

// 映射默认按get方式请求
@Configuration
public class MyWebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
