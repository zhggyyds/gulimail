package com.hao.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.utils.PageUtils;
import com.hao.common.vo.auth.SocialUserVo;
import com.hao.gulimall.member.entity.MemberEntity;
import com.hao.gulimall.member.exception.ExistPhoneException;
import com.hao.gulimall.member.exception.ExistUsernameException;
import com.hao.gulimall.member.vo.UserLoginVo;
import com.hao.gulimall.member.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 12:29:41
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo member);

    void checkPhoneUnique(String phone) throws ExistPhoneException;

    void checkUsernameUnique(String username) throws ExistUsernameException;

    MemberEntity login(UserLoginVo userLoginVo);

    MemberEntity login(SocialUserVo socialUserVo);
}

