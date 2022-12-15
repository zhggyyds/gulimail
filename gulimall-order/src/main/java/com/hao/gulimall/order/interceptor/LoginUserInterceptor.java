package com.hao.gulimall.order.interceptor;

import com.hao.common.constant.auth.AuthConstant;
import com.hao.common.vo.auth.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.order.interceptor
 * @Description:
 * @date 2022/11/29 16:02
 **/

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 放行不需要登陆的远程服务请求
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/order/order/status/**", uri);
        boolean match1 = antPathMatcher.match("/pay/**", uri);
        if (match || match1){
            return true;
        }

        HttpSession session = request.getSession();
        // 获取登录用户信息
        MemberResponseVo attribute = (MemberResponseVo) session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute != null) {
            // 已登录，放行
            // 封装用户信息到threadLocal
            threadLocal.set(attribute);
            return true;
        } else {
            // 未登录，跳转登录页面
            session.setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.gulimall.com");
            return false;
        }
    }
}
