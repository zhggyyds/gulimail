package com.hao.gulimall.product.app;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hao.gulimall.product.entity.SkuInfoEntity;
import com.hao.gulimall.product.service.SkuInfoService;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.R;



/**
 * sku信息
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
@RestController
@RequestMapping("product/skuinfo")
public class SkuInfoController {

    @Autowired
    private SkuInfoService skuInfoService;

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("product:skuinfo:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = skuInfoService.queryPageInfo(params);

        return R.ok().put("page", page);
    }

    @GetMapping("/price/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId")Long skuId){
        SkuInfoEntity skuInfoEntity = skuInfoService.getById(skuId);
        return skuInfoEntity.getPrice();
    }

    /**
     * 信息
     */
    @GetMapping("/info/{skuId}")
//   @RequiresPermissions("product:skuinfo:info")
    public R info(@PathVariable("skuId") Long skuId){
		SkuInfoEntity skuInfo = skuInfoService.getById(skuId);

        return R.ok().put("skuInfo", skuInfo);
    }

    /**
     * 查询商品集合
     */
    @PostMapping("/infos")
    public R infos(@RequestBody List<Long> skuIds) {
        List<SkuInfoEntity> skuInfos = skuInfoService.getByIds(skuIds);
        return R.ok().setData(skuInfos);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("product:skuinfo:save")
    public R save(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.save(skuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:skuinfo:update")
    public R update(@RequestBody SkuInfoEntity skuInfo){
		skuInfoService.updateById(skuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:skuinfo:delete")
    public R delete(@RequestBody Long[] skuIds){
		skuInfoService.removeByIds(Arrays.asList(skuIds));

        return R.ok();
    }

}
