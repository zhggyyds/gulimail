package com.hao.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.hao.common.exception.BizCodeEnume;
import com.hao.common.vo.auth.SocialUserVo;
import com.hao.gulimall.member.exception.ExistPhoneException;
import com.hao.gulimall.member.exception.ExistUsernameException;
import com.hao.gulimall.member.vo.UserLoginVo;
import com.hao.gulimall.member.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hao.gulimall.member.entity.MemberEntity;
import com.hao.gulimall.member.service.MemberService;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.R;



/**
 * 会员
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 12:29:41
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//   @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id){
		MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }


    /**
     * 注册
     */
    @PostMapping("/register")
//   @RequiresPermissions("member:member:save")
    public R register(@RequestBody UserRegisterVo member){

        try {
            memberService.register(member);
        }catch (ExistUsernameException existUsernameException){
            return  R.error(BizCodeEnume.USER_EXIST_EXCEPTION.getCode(),BizCodeEnume.USER_EXIST_EXCEPTION.getMsg());
        }catch (ExistPhoneException existPhoneException){
            return  R.error(BizCodeEnume.PHONE_EXIST_EXCEPTION.getCode(),BizCodeEnume.PHONE_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }

    /*
    * 登陆
    * */
    @PostMapping("/login")
    public R Login(@RequestBody UserLoginVo userLoginVo){

        MemberEntity memberEntity =  memberService.login(userLoginVo);
        if(memberEntity != null){
            return R.ok().setData(memberEntity); // 返回用户数据给请求的服务使用
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }

    }

    /*
     * 微博登陆
     * */
    @PostMapping("/oauth2/login")
    public R oauth2Login(@RequestBody SocialUserVo socialUserVo){

        MemberEntity memberEntity =  memberService.login(socialUserVo);
        if(memberEntity != null){
            return R.ok().setData(memberEntity); // 返回用户数据给请求的服务使用
        }else {
            return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
        }

    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member){
		memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member){
		memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids){
		memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
