package com.hao.common.exception;

/**
 * @author zhouhao
 * @PackageName:com.hao.common.exception
 * @Description:
 * @date 2022/12/5 13:37
 **/
public class VerifyPriceException extends RuntimeException {
    public VerifyPriceException(){
        super("验价异常");
    }
}
