package com.hao.gulimall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.ware.config
 * @Description:
 * @date 2022/9/14 19:56
 **/
@Configuration
@EnableTransactionManagement
@MapperScan("com.hao.gulimall.ware.dao")
public class MybatisPlusConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //设置请求的页面大于最大页后操作，true调回到首页，faise 继卖请求 默认false
        paginationInterceptor.setOverflow(true);
        // 设置最大单页限制数量，野认 50日条，-1 不受限制
        paginationInterceptor.setLimit(10);
        return paginationInterceptor;
    }

}
