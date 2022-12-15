package com.hao.gulimall.member.feign;

import com.hao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.member.feign
 * @Description:
 * @date 2022/12/10 14:38
 **/

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @PostMapping("/order/order/listWithItems")
    R listWithItems(@RequestBody Map<String, Object> params);
}
