package com.hao.cart.interceptor;

import com.hao.cart.to.UserInfoTo;
import com.hao.common.constant.auth.AuthConstant;
import com.hao.common.constant.cart.CartConstant;
import com.hao.common.vo.auth.MemberResponseVo;
import org.apache.commons.lang.ArrayUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @author zhouhao
 * @PackageName:com.hao.cart.interceptor
 * @Description: 判断用户登陆状态，为临时购物车添加cookie标识
 * @date 2022/11/23 16:10
 *
 *
 * user-key这个cookie的作用 - 将临时购物车和登陆后的购物车关联起来，确保后续登陆以后将临时购物车的商品添加进用户的购物车
 *
 * 用户没有登陆 ，有cookie
 * 用户登陆了，没有cookie
 * 用户没有登陆，没有cookie
 * 用户登陆了，有cookie
 **/
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>(); // 线程内数据共享

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        // 查询用户的登陆信息
        MemberResponseVo memberResponseVo = (MemberResponseVo)session.getAttribute(AuthConstant.LOGIN_USER);

        UserInfoTo userInfoTo = new UserInfoTo();
        // 已登陆
        if (memberResponseVo != null){
            userInfoTo.setUserId(memberResponseVo.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (ArrayUtils.isNotEmpty(cookies)){
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(CartConstant.TEMP_USER_COOKIE_NAME)){
                    userInfoTo.setUserKey(cookie.getValue()); // 找到user-key
                    break;
                }
            }
        }

        if (StringUtils.isEmpty(userInfoTo.getUserKey())){
            // 如果现在UserKey仍为空，手动创建
            userInfoTo.setUserKey(UUID.randomUUID().toString());
            userInfoTo.setFirstAddCookie(true);
        }

        threadLocal.set(userInfoTo);// 将user信息放入，够后续同线程的方法使用
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = threadLocal.get();
        if (userInfoTo.isFirstAddCookie()){ // 首次创建user-key则将cookie回应给浏览器
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            cookie.setDomain("gulimall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }
}
