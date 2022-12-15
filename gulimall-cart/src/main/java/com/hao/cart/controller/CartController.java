package com.hao.cart.controller;

import com.hao.cart.service.CartService;
import com.hao.cart.vo.CartItemVo;
import com.hao.cart.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @author zhouhao
 * @PackageName:com.hao.cart.controller
 * @Description:
 * @date 2022/11/23 17:25
 **/

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @GetMapping("/")
    public String getCart(Model model) throws ExecutionException, InterruptedException {

        CartVo cart =  cartService.getCart();

        model.addAttribute("cart",cart);

        return "index";
    }

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("num") Integer num, RedirectAttributes attributes) throws ExecutionException, InterruptedException {

        cartService.addToCart(skuId,num);
        attributes.addAttribute("skuId",skuId);
        return "redirect:http://cart.gulimall.com/addToCartSuccess";
    }

    @GetMapping("/addToCartSuccess")
    /*
     * @description 防止刷新添加成功页面后再次添加商品到购物车从而数量变多
     * @date 2022/11/24 11:12
     * @param skuId
     * @return java.lang.String
     */
    public String addToCartSuccess(@RequestParam Long skuId, Model model){

        CartItemVo itemVo = cartService.getCartItem(skuId);
        model.addAttribute("item",itemVo);
        return "success";
    }

    /**
     * 更改购物车商品选中状态
     */
    @GetMapping(value = "/checkItem")
    public String checkItem(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "checked") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.gulimall.com";
    }

    /**
     * 改变商品数量
     */
    @GetMapping(value = "/countItem")
    public String countItem(@RequestParam(value = "skuId") Long skuId,
                            @RequestParam(value = "num") Integer num) {
        cartService.changeItemCount(skuId,num);
        return "redirect:http://cart.gulimall.com";
    }

    /**
     * 删除商品信息
     */
    @GetMapping(value = "/deleteItem")
    public String deleteItem(@RequestParam("skuId") Integer skuId) {
        cartService.deleteIdCartInfo(skuId);
        return "redirect:http://cart.gulimall.com";
    }

    @GetMapping( "/checkedUserCartItems")
    @ResponseBody
    public List<CartItemVo> getCurrentCartItems(){
        return cartService.getCheckedCartItems();
    }
}
