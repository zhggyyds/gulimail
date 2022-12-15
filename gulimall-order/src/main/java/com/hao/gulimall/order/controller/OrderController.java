package com.hao.gulimall.order.controller;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hao.gulimall.order.entity.OrderEntity;
import com.hao.gulimall.order.service.OrderService;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.R;



/**
 * 订单
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 11:50:23
 */
@RestController
@RequestMapping("order/order")
public class OrderController {
    @Autowired
    private OrderService orderService;


    /**
     * 通过OrderSn 获取订单详情
     */
    @GetMapping("/status/{orderSn}")
    public R getOrderByOrderSn(@PathVariable("orderSn") String orderSn) {
        OrderEntity order = orderService.getOrderByOrderSn(orderSn);
        return R.ok().setData(order);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("order:order:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = orderService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 查询某用户的订单列表和商品详情信息
     */
    @PostMapping("/listWithItems")
//    @RequiresPermissions("order:order:list")
    public R listWithItems(@RequestBody Map<String, Object> params){
        PageUtils page = orderService.queryPageWithItems(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//   @RequiresPermissions("order:order:info")
    public R info(@PathVariable("id") Long id){
		OrderEntity order = orderService.getById(id);

        return R.ok().put("order", order);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("order:order:save")
    public R save(@RequestBody OrderEntity order){
		orderService.save(order);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("order:order:update")
    public R update(@RequestBody OrderEntity order){
		orderService.updateById(order);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("order:order:delete")
    public R delete(@RequestBody Long[] ids){
		orderService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
