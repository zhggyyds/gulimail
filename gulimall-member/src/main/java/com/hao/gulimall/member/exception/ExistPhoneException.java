package com.hao.gulimall.member.exception;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth.exception
 * @Description:
 * @date 2022/11/14 09:21
 **/
public class ExistPhoneException extends RuntimeException {

    public ExistPhoneException(){
        super("手机号已经存在");
    }
}
