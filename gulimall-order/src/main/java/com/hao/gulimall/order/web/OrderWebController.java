package com.hao.gulimall.order.web;

import com.hao.common.exception.NoStockException;
import com.hao.common.exception.VerifyPriceException;
import com.hao.common.vo.order.OrderConfirmVo;
import com.hao.common.vo.order.OrderSubmitVo;
import com.hao.gulimall.order.service.OrderService;
import com.hao.gulimall.order.vo.SubmitOrderResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.ExecutionException;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.order.web
 * @Description:
 * @date 2022/11/29 16:09
 **/

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @GetMapping("/toTrade")
    public String toTrade(Model model, HttpServletRequest request) throws ExecutionException, InterruptedException {

        OrderConfirmVo confirmVo = orderService.getOrderConfirmData();
        model.addAttribute("confirmOrderData", confirmVo);

        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes attributes) throws Exception {

        try {
            // 创建订单成功，跳转收银台
            SubmitOrderResponseVo orderVO = orderService.submitOrder(orderSubmitVo);
            model.addAttribute("submitOrderResp", orderVO);// 封装VO订单数据，供页面解析[订单号、应付金额]
            return "pay";
        } catch (Exception e) {
            // 下单失败回到订单结算页
            if (e instanceof VerifyPriceException) {
                String message = ((VerifyPriceException) e).getMessage();
                attributes.addFlashAttribute("msg", "下单失败" + message);
            }
            else if (e instanceof NoStockException) {
                String message = ((NoStockException) e).getMessage();
                attributes.addFlashAttribute("msg", "下单失败" + message);
            }
            return "redirect:http://order.gulimall.com/toTrade";
        }
    }
}
