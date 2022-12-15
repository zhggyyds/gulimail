package com.hao.gulimall.member.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.member.vo
 * @Description:
 * @date 2022/11/14 09:17
 **/
@Data
public class UserRegisterVo {

    private String userName;

    private String password;

    private String phone;
}
