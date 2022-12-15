package com.hao.gulimall.seckill.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: wanzenghui
 * @createTime: 2020-07-09 19:23
 */
// 开启异步任务
@EnableAsync
// 开启定时任务
@EnableScheduling
// 配置类
@Configuration
public class ScheduledConfig {

}
