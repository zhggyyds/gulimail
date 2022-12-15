package com.hao.gulimall.auth.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth.vo
 * @Description: JSR303后端数据校验
 * @date 2022/11/12 22:01
 **/
@Data
public class UserRegisterVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 6, max = 19, message="用户名长度必须是6-18字符")
    private String userName;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "密码长度必须是6—18位字符")
    private String password;

    @NotEmpty(message = "手机号必须填写")
    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$", message = "手机号格式不正确")
    private String phone;

    @NotEmpty(message = "验证码必须填写")
    private String code;

}
