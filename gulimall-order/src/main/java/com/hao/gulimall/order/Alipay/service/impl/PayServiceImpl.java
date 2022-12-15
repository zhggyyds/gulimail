package com.hao.gulimall.order.Alipay.service.impl;

import com.hao.gulimall.order.Alipay.service.PayService;
import com.hao.gulimall.order.Alipay.vo.PayAsyncVo;
import com.hao.gulimall.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.order.Alipay.service.impl
 * @Description:
 * @date 2022/12/10 17:15
 **/

@Slf4j
@Service("payService")
public class PayServiceImpl implements PayService {

    @Autowired
    OrderService orderService;

    @Override
    public String handlePayResult(PayAsyncVo aliVo) {
        // 获取支付状态
        String tradeStatus = aliVo.getTrade_status();
        // 检查支付状态
        if (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")) {
            try {
                orderService.handlePayResult(aliVo);
                return "success";
            }catch (Exception e){
                log.error(e.getMessage());
                return "fail";
            }
        }
        return "fail";
    }
}
