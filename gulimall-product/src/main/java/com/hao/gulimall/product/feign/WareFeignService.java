package com.hao.gulimall.product.feign;


import com.hao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.feign
 * @Description:
 * @date 2022/9/19 13:18
 **/


@FeignClient("gulimall-ware")
public interface WareFeignService {

    /*
     * @description 查询商品库存
     * @date 2022/10/9 13:13
     * @param skuIds
     * @return com.hao.common.utils.R
     */
    @PostMapping("ware/waresku/hasStock")
    R getSkuHasStocks(List<Long> skuIds);
}

