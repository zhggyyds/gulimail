package com.hao.gulimall.product.feign;

import com.hao.common.utils.R;
import com.hao.gulimall.product.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// TODO 整合使用feign来使用降级熔断
@FeignClient(value = "gulimall-seckill", fallback = SeckillFeignServiceFallBack.class,
        configuration = SeckillFeignServiceFallBack.class)
public interface SeckillFeignService {

    /**
     * 根据skuId查询商品是否参加秒杀活动
     */
    @GetMapping(value = "/sku/seckill/{skuId}")
    R getSkuSecKilInfo(@PathVariable("skuId") Long skuId);

}
