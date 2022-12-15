package com.hao.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.config
 * @Description:让线程池配置类能够进行外部的配置
 * @date 2022/11/10 15:27
 **/
@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {
    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;
}
