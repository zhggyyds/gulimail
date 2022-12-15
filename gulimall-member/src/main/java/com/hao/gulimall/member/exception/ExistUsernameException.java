package com.hao.gulimall.member.exception;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.auth.exception
 * @Description:
 * @date 2022/11/14 09:22
 **/
public class ExistUsernameException extends RuntimeException{

    public ExistUsernameException(){
        super("用户名已经存在");
    }

}
