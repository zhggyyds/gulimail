package com.hao.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.hao.common.vo.order.PayVo;
import com.hao.gulimall.order.Alipay.AlipayTemplate;
import com.hao.gulimall.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.order.web
 * @Description:
 * @date 2022/12/9 20:29
 **/

@Controller
public class PayWebController {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    /*
     * @description 调用支付宝支付功能
     * @date 2022/12/9 21:59
     * @param orderSn
     * @return java.lang.String
     */
    @ResponseBody
    @GetMapping(value = "/aliPay",produces = "text/html") // produces设置返回数据格式
    public String payOrder(@RequestParam("orderSn") String orderSn) throws AlipayApiException {
        PayVo payVo = orderService.getPayVo(orderSn);
        // 接返回一个表单 action是自己配置的return_url，并且这个表单会被浏览器立即执行
        // 因为是在自己的电脑上测试，返回的表单action可以直接访问，上线后要改成公网可访问的地址
        return alipayTemplate.pay(payVo);
    }
}
