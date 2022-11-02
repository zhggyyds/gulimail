package com.hao.gulimall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.search
 * @Description:
 * @date 2022/9/17 19:51
 **/

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class GuliMallEsApplication {
    public static void main(String[] args) {
        SpringApplication.run(GuliMallEsApplication.class, args);

    }
}
