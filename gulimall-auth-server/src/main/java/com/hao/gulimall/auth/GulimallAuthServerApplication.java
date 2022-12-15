package com.hao.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth
 * @Description:
 * @date 2022/11/11 10:01
 **/

/*
*
* TODO session原理 装饰者模式+自动续期 通过filter 对原始的请求和响应进行包装，让getSession获得封装后的session ，从而可以操作redis

1)、@EnablcRedisHttpSession导入RedisHttpSessionConfiguration配置
	1、给容器中添加了一个组件
		SessionRepository is extended by【RedisIndexedSessionRepository】-> 用于操作redis -> session的增删改查操作
	2、SessionRepositoryFilter implement Filter:session 每个请求过来都必须经过filter
		1、创建的时侯，从容器中获取到了sessionRepository;(自动注入）
		2、包装原始的request，response。SessionRepositoryRequestWrapper，SessionRepositoryResponseWrapper
		3、以后获取session。-> 从被包装过的请求中查找 request.getSession();
		4、wrappedRequest.getSession(); 如果session中不存在，SessionRepository 中获取到getById(xx)
2）、Spring Session 会给redis中的session数据自动延期

Spring Session核心方法：SessionRepositoryFilter过滤器
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        request.setAttribute(SESSION_REPOSITORY_ATTR, this.sessionRepository);
        // 装饰者模式，包装request和response，然后将包装后的request和response对象放行
        // 然后request和response被换成了SessionRepositoryRequestWrapper和SessionRepositoryResponseWrapper对象
        SessionRepositoryFilter<S>.SessionRepositoryRequestWrapper wrappedRequest = new SessionRepositoryFilter.SessionRepositoryRequestWrapper(request, response);
        SessionRepositoryFilter.SessionRepositoryResponseWrapper wrappedResponse = new SessionRepositoryFilter.SessionRepositoryResponseWrapper(wrappedRequest, response);

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            wrappedRequest.commitSession();
        }
    }
*
* */


@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableRedisHttpSession
public class GulimallAuthServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServerApplication.class, args);
    }

}
