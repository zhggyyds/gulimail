package com.hao.gulimall.product.exception;

import com.hao.common.exception.BizCodeEnume;
import com.hao.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.exception
 * @Description: 统一处理数据校验产生的问题，解耦在controller中异常处理代码和业务代码,并返回自定义的更容易前端阅读的code码和提示
 * @date 2022/8/23 13:18
 **/

@Slf4j
@RestControllerAdvice(basePackages = "com.hao.gulimall.product.controller")
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class) // 也可以返回ModelAndView
    public R handleValidException(MethodArgumentNotValidException exception){
        Map<String,String> map=new HashMap<>();

        //从传入的exception中 获取数据校验的错误结果
        BindingResult bindingResult = exception.getBindingResult();
        bindingResult.getFieldErrors().forEach(fieldError -> {
            String message = fieldError.getDefaultMessage();
            String field = fieldError.getField();
            map.put(field,message);
        });

        log.error("数据校验出现问题 - {},异常类型 - w{}",exception.getMessage(),exception.getClass());

        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(), BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data",map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R generalHandleValidException(Throwable exception){
       return R.error(BizCodeEnume.UNKNOW_EXCEPTION.getCode(),BizCodeEnume.UNKNOW_EXCEPTION.getMsg());
    }
}
