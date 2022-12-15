package com.hao.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hao.common.utils.HttpUtils;
import com.hao.common.vo.auth.SocialUserVo;
import com.hao.gulimall.member.exception.ExistPhoneException;
import com.hao.gulimall.member.exception.ExistUsernameException;
import com.hao.gulimall.member.vo.UserLoginVo;
import com.hao.gulimall.member.vo.UserRegisterVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.member.dao.MemberDao;
import com.hao.gulimall.member.entity.MemberEntity;
import com.hao.gulimall.member.service.MemberService;

import javax.xml.ws.Action;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberService memberService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /*
     * @description 注册功能实现
     * @date 2022/11/14 09:16
     * @param TODO UserRegisterVo 前不用添加@RequestBody,因为表单提交的不是Json数据
     * @return null
     */
    @Override
    public void register(UserRegisterVo userRegisterVo) throws ExistPhoneException,ExistUsernameException{
        MemberEntity memberEntity = new MemberEntity();

        // 检查用户名和手机号的唯一性，如果存在就抛出异常
        checkUsernameUnique(userRegisterVo.getUserName());
        checkPhoneUnique(userRegisterVo.getPhone());

        memberEntity.setUsername(userRegisterVo.getUserName());
        memberEntity.setMobile(userRegisterVo.getPhone());

        // 密码加密存储,spring的密码加密，比MD5的盐值加密省略了存储盐值字段
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(userRegisterVo.getPassword());
        memberEntity.setPassword(encode);

        // 将用户信息存入数据库
        this.baseMapper.insert(memberEntity);

    }

    @Override
    public void checkPhoneUnique(String phone) {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count>0){
            throw new ExistPhoneException();
        }
    }

    @Override
    public void checkUsernameUnique(String username) {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count>0){
            throw new ExistUsernameException();
        }
    }

    @Override
    public MemberEntity login(UserLoginVo userLoginVo) {
        String account = userLoginVo.getAccount();
        String password = userLoginVo.getPassword();

        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("username", account).or().eq("mobile", account));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (memberEntity != null && encoder.matches(password, memberEntity.getPassword())){
            // 登陆成功
            return memberEntity;
        }
        //登陆失败
        return null;
    }

    @Override
    public MemberEntity login(SocialUserVo socialUserVo) {
        // 1.查询是否是第一次登陆
        MemberEntity memberEntity = this.baseMapper.selectOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUserVo.getUid()));

        if (memberEntity != null){
            // 2。已注册,更新token和过期时间信息
            memberEntity.setAccessToken(socialUserVo.getAccessToken());
            memberEntity.setExpiresIn(socialUserVo.getExpiresIn());
            baseMapper.updateById(memberEntity);
            return memberEntity;//返回给controller
        }else {
            // 3.未注册
            MemberEntity member = new MemberEntity();
            try { // 查询结果不影响注册结果，所以使用try/catch

                // 查询当前社交用户的社交账号信息，封装会员uid 和 访问令牌
                Map<String, String> queryMap = new HashMap<>();
                queryMap.put("access_token", socialUserVo.getAccessToken());
                queryMap.put("uid", socialUserVo.getUid());
                // 发送请求获得微博用户的信息
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", new HashMap<String, String>(), queryMap);

                if (response.getStatusLine().getStatusCode() == 200) {
                    //查询成功
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);// 将查询到的json数据封装为json对象
                    String name = jsonObject.getString("name"); // 查询json对象中name属性
                    String gender = jsonObject.getString("gender");
                    String profileImageUrl = jsonObject.getString("profile_image_url");
                    // 封装注册信息
                    member.setNickname(name);
                    member.setGender("m".equals(gender) ? 1 : 0);
                    member.setHeader(profileImageUrl);
                    member.setCreateTime(new Date());
                }

            } catch (Exception e) {
                log.error("获取微博用户信息失败，但是注册成功");
            }

            // 即使出现网络异常也要进行注册，减少网络交互
            member.setSocialUid(socialUserVo.getUid());
            member.setAccessToken(socialUserVo.getAccessToken());
            member.setExpiresIn(socialUserVo.getExpiresIn());

            //把用户信息插入到数据库中 - 注册
            baseMapper.insert(member);

            return member;//返回给controller
        }
    }

}