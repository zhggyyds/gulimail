package com.hao.gulimall.seckill.feign;

import com.hao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.seckill.feign
 * @Description:
 * @date 2022/12/11 16:32
 **/
@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 查询最近三天需要参加秒杀的场次+商品
     */
    @GetMapping(value = "/coupon/seckillsession/Latest3DaySession")
    R getLatest3DaySession();

}