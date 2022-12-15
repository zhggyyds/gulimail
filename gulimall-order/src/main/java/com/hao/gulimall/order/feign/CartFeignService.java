package com.hao.gulimall.order.feign;

import com.hao.common.vo.cart.CartItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 购物车系统
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    /**
     * 查询当前用户购物车选中的商品项
     */
    @GetMapping(value = "/checkedUserCartItems")
    List<CartItemVo> getCheckedCartItems();

}
