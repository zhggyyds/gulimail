package com.hao.cart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.config
 * @Description:让线程池配置类能够进行外部的配置
 * @date 2022/11/10 15:27
 **/

// 设置线程池配置项，能够使用元数据方式配置
@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
