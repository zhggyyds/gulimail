package com.hao.gulimall.order.feign;


import com.hao.common.to.order.WareSkuLockTo;
import com.hao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WmsFeignService {

    // 库存查询
    @PostMapping("/ware/waresku/hasStock")
     R getSkuHasStocks(@RequestBody List<Long> skuIds);

    /**
     * 查询运费和收货地址信息
     */
    @GetMapping(value = "/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") String addrId);


    /**
     * 锁定库存
     * @param wareSkuLockTo
     */
    @PostMapping(value = "/ware/waresku/lock/order")
    R lockStock(@RequestBody WareSkuLockTo wareSkuLockTo);
}
