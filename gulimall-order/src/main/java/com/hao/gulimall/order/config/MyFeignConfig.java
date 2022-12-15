package com.hao.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.order.config
 * @Description: 远程调用时如果不为其配置 拦截器，那么会丢失请求头中的数据
 * @date 2022/11/29 21:26
 **/

// feign调用本质是使用Java代码创建了一个新的请求，用于远程调用，相当于请求头中是没有数据的，feign调用时会先通过拦截器构造请求头

@Configuration
public class MyFeignConfig {


    /**
     * 注入拦截器
     * feign调用时会通过拦截器构造请求头，封装cookie解决远程调用时无法获取springsession
     */
    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                // 1、使用RequestContextHolder拿到原生请求的请求头信（下文环境保持器）

                // 从ThreadLocal中获取请求头（要保证feign调用与controller请求处在同一线程环境）
                // 将RequestAttributes 强转为其实现类 ServletRequestAttributes 再使用
                // 底层使用的是ThreadLocal进行数据传递使用，这是框架为我们做好的事
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (requestAttributes != null){
                    // 2.获得原始request
                    HttpServletRequest request = requestAttributes.getRequest();
                    if (request != null){
                        //获取cookie
                        String cookie = request.getHeader("Cookie");
                        // 同步cookie
                        requestTemplate.header("Cookie",cookie);
                    }
                }


            }
        };
    }

}
