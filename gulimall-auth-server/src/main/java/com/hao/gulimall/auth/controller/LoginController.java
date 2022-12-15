package com.hao.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.hao.common.constant.auth.AuthConstant;
import com.hao.common.exception.BizCodeEnume;
import com.hao.common.utils.R;
import com.hao.common.vo.auth.MemberResponseVo;
import com.hao.gulimall.auth.feign.MemberFeignService;
import com.hao.gulimall.auth.feign.ThirdPartyFeign;
import com.hao.gulimall.auth.vo.UserLoginVo;
import com.hao.gulimall.auth.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth.controller
 * @Description:
 * @date 2022/11/11 16:23
 **/

@Controller
public class LoginController {

    @Autowired
    ThirdPartyFeign thirdPartyFeign;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MemberFeignService memberFeignService;


    /**
     * 访问登录页面
     * 登录状态自动跳转首页
     */
    @GetMapping(value = "/")
    public String loginPage(HttpSession session) {
        // 判断是否登录状态
        Object attribute = session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute == null) {
            // 未登录，返回登录页资源
            return "index";
        } else {
            // 已登录
            return "redirect:http://gulimall.com";
        }
    }


    /*
     * @description 发送验证码
     * @date 2022/11/14 22:48
     * @param null
     * @return null
     */
    @GetMapping("/sms/sendcode")
    @ResponseBody
    public R sendCode(@RequestParam("phone") String phone){
        // 1.判断60秒之后发送的验证码请求，防刷
        String _code = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX+phone);

        // 2.当缓存中存在验证码并切距离上次时间超小于60s，返回error
        if(!StringUtils.isEmpty(_code) && System.currentTimeMillis() -Long.parseLong(_code.split("_")[1])<60000){
            return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(),BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
        }

        String code = UUID.randomUUID().toString().substring(0, 5);

        // 存入缓存方便验证码对比操作    key :前缀 + 手机号 , value :code_存入的时间
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX+phone,code+"_"+System.currentTimeMillis(),10, TimeUnit.MINUTES);

        // 发送验证码
        thirdPartyFeign.sendCode(phone,code);
        return R.ok();
    }

    /**
     * 注册接口
     *
     * @param userRegisterVo        接收注册信息
     * @param bindingResult     接收参数校验结果
     * @param attributes 用于重定向保存数据（原理：使用session，重定向请求后根据cookie拿到session的数据）TODO 分布式session
     */
    @PostMapping("/register")
    public String register(@Valid UserRegisterVo userRegisterVo, BindingResult bindingResult, RedirectAttributes attributes){
        // 1.后端数据校验不通过
        if (bindingResult.hasErrors()){
            HashMap<String, String> errMap = new HashMap<>();
            bindingResult.getFieldErrors().forEach(err-> errMap.put(err.getField(), err.getDefaultMessage()));
            attributes.addFlashAttribute("errors", errMap);
            return "redirect:http://auth.gulimall.com/reg.html";// 采用重定向有一定防刷功能
            // 1、return "redirect:http://auth.gulimall.com/reg.html"【采用】 TODO 重定向Get请求【配合RedirectAttributes共享数据】
            // 2、return "redirect:http:/reg.html"                   【采用】 重定向Get请求，省略当前服务url【配合RedirectAttributes共享数据】
            // 3、return "redirect:/reg.html"                                重定向Get请求，使用视图控制器拦截请求并映射reg视图【配合RedirectAttributes共享数据】【bug：会以ip+port来重定向】
            // 4、return "forward:http://auth.gulimall.com/reg.html";        请求转发与当前请求方式一致（Post请求）【配合Model共享数据】【异常404：当前/reg.html不存在post请求】
            // 5、return "forward:http:/reg.html";                           请求转发与当前请求方式一致（Post请求），省略当前服务url 【配合Model共享数据】【异常404：当前/reg.html不存在post请求】
            // 6、return "forward:/reg.html";                                请求转发与当前请求方式一致（Post请求），使用视图控制器拦截请求并映射reg视图【配合Model共享数据】【异常405：Request method 'POST' not supported，视图控制器必须使用GET请求访问，而当前请求转发使用post方式，导致异常】
            // 7、return "reg";                                              视图解析器前后拼串查找资源返回【配合Model共享数据】，使用的请求转发，会恶意刷新重复提交表单的风险
        }

        String code = userRegisterVo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());

        // 2.验证码校验通过
        if(!StringUtils.isEmpty(redisCode) && code.equals(redisCode.split("_")[0])){
            // 删除验证码 - 令牌机制
            redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + userRegisterVo.getPhone());

            R r = memberFeignService.register(userRegisterVo);
            if (r.getCode() == 0){
                // 注册成功
                return "redirect:http://auth.gulimall.com/";
            }else{
                // 4.注册失败 - 用户名或者手机号重复
                String msg = r.getData("msg", new TypeReference<String>() {
                });
                HashMap<String, String> errMap = new HashMap<>();
                errMap.put("msg",msg);
                attributes.addFlashAttribute("errors", errMap);
                return "redirect:http://auth.gulimall.com/reg.html";// 采用重定向有一定防刷功能
            }

        }else{
            // 3.验证码错误 或 过期
            HashMap<String, String> errMap = new HashMap<>();
            errMap.put("code","验证码校验错误");
            attributes.addFlashAttribute("errors", errMap);
            return "redirect:http://auth.gulimall.com/reg.html";// 采用重定向有一定防刷功能
        }
    }


    @PostMapping("/login")
    /*
     * @description TODO 登陆  表单提交的数据是键值对，无需使用@RequestBody.但是远程调用是使用json数据传输，需要添加注解
     * @date 2022/11/15 13:45
     * @param userLoginVo
     * @return java.lang.String
     */
    public String login(UserLoginVo userLoginVo, RedirectAttributes attributes, HttpSession session){
        // 远程方法调用进行登陆
        R r = memberFeignService.login(userLoginVo);
        if(r.getCode() == 0){
            // 登陆成功
            MemberResponseVo data = r.getData("data", new TypeReference<MemberResponseVo>() {
            });
            session.setAttribute(AuthConstant.LOGIN_USER, data);
            return "redirect:http://gulimall.com";
        }else {
            // 登陆失败
            String msg = r.getData("msg", new TypeReference<String>() {
            });
            HashMap<String, String> errMap = new HashMap<>();
            errMap.put("msg",msg);
            attributes.addFlashAttribute("errors", errMap);
            return "redirect:http://auth.gulimall.com";
        }

    }
}
