package com.hao.gulimall.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.hao.common.constant.auth.AuthConstant;
import com.hao.common.utils.HttpUtils;
import com.hao.common.utils.R;
import com.hao.common.vo.auth.MemberResponseVo;
import com.hao.common.vo.auth.SocialUserVo;
import com.hao.gulimall.auth.feign.MemberFeignService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**s
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth.controller
 * @Description:
 * @date 2022/11/16 17:45
 **/

@Slf4j
@Controller
public class OAuth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/weibo/Oauth2/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {

        HashMap<String, String> map = new HashMap<>();
        map.put("client_id", "2142124255");
        map.put("client_secret", "0ac6c78043157b603296d1f79e4ea9f1");
        map.put("grant_type", "authorization_code");
        map.put("redirect_uri", "http://auth.gulimall.com/weibo/Oauth2/success");
        map.put("code", code);

        // post请求 code换取access token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token"
                , "post", new HashMap<String,String>(), new HashMap<String,String>(), map);

        if(response.getStatusLine().getStatusCode() == 200){
            String json = EntityUtils.toString(response.getEntity()); // 获取响应体中json格式的数据
            SocialUserVo socialUserVo = JSONObject.parseObject(json, SocialUserVo.class); // 封装成对应vo类
            R r = memberFeignService.oauth2Login(socialUserVo); // 远程服务调用进行微博登陆
            if (r.getCode() == 0){
                // 登陆成功
                MemberResponseVo memberResponseVo = r.getData("data", new TypeReference<MemberResponseVo>() {
                });
                session.setAttribute(AuthConstant.LOGIN_USER,memberResponseVo);// 将查询到的用户信息放入session中让前端使用
                return "redirect:http://gulimall.com"; //防刷
            }else {
                // 登陆失败
                return "redirect:http://auth.gulimall.com";
            }
        }else {
            // 换取令牌失败
            return "redirect:http://auth.gulimall.com";
        }
    }

}
