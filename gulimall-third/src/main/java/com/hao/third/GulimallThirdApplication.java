package com.hao.third;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author zhouhao
 * @PackageName:com.hao.third
 * @Description:
 * @date 2022/8/22 13:56
 **/

@EnableDiscoveryClient
@SpringBootApplication
public class GulimallThirdApplication {
    public static void main(String[] args) {
        SpringApplication.run(GulimallThirdApplication.class, args);
    }
}
