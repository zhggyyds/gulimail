package com.hao.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.hao.gulimall.order.Alipay.AlipayTemplate;
import com.hao.gulimall.order.Alipay.service.PayService;
import com.hao.gulimall.order.Alipay.vo.PayAsyncVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.order.listener
 * @Description: 监听支付宝支付成功的异步回掉
 * @date 2022/12/10 16:47
 **/

@Slf4j
@RestController
public class OrderPayListener {

    @Autowired
    PayService payService;

    @Autowired
    AlipayTemplate alipayTemplate;

    @PostMapping(value = "/pay/notify")

    public String handleAlipayed(PayAsyncVo payAsyncVo, HttpServletRequest request) throws AlipayApiException {
        //处理支付宝异步通知结果
        log.info("收到支付宝异步消息");
        String res = "error";
        // 验签，保证请求中途没有被修改
        Boolean verify = alipayTemplate.verify(request);
        if (verify){
            log.info("签名验证成功");
            res = payService.handlePayResult(payAsyncVo);
        }
        return res;// 返回success，支付宝将不再异步回调
    }
}
