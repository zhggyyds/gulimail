package com.hao.gulimall.auth.feign;

import com.hao.common.utils.R;
import com.hao.common.vo.auth.SocialUserVo;
import com.hao.gulimall.auth.vo.UserLoginVo;
import com.hao.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth.feign
 * @Description:
 * @date 2022/11/14 22:32
 **/

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/register")
    R register(@RequestBody UserRegisterVo member);

    @PostMapping("/member/member/login")
    R login(@RequestBody UserLoginVo user);

    @PostMapping("/member/member/oauth2/login")
    R oauth2Login(@RequestBody SocialUserVo socialUserVo);
}
