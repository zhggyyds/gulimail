package com.hao.gulimall.order.Alipay.service;

import com.hao.gulimall.order.Alipay.vo.PayAsyncVo;
import org.springframework.stereotype.Service;


public interface PayService {
    /*
     * @description 处理支付宝的异步通知
     * @date 2022/12/10 21:18
     * @param null
     * @return null
     */
    String handlePayResult(PayAsyncVo payAsyncVo);
}
