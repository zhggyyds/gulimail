package com.hao.gulimall.search.feign;

import com.hao.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.search.feign
 * @Description:
 * @date 2022/11/1 21:27
 **/
@FeignClient("gulimall-product")
public interface ProductFeignService {

    /**
     * 查询属性
     */
    @GetMapping("/product/attr/info/{attrId}")
    R attrInfo(@PathVariable("attrId") Long attrId);


    // 查询多个品牌信息
    @GetMapping("/product/brand/infoByIds")
    public R getBrandByIds(@RequestParam("brandIds") List<Long> brandIds);
}
