package com.hao.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.hao.common.valid.AddGroup;
import com.hao.common.valid.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.hao.gulimall.product.entity.BrandEntity;
import com.hao.gulimall.product.service.BrandService;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.R;



/**
 * 品牌
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
//   @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId){
		BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 信息
     */
    @Cacheable(value = "brands",key = "'brands:'+#root.args[0]")
    @GetMapping("/infoByIds")
//   @RequiresPermissions("product:brand:info")
    public R getBrandByIds(@RequestParam("brandIds") List<Long> brandIds){
        List<BrandEntity> brands = brandService.getBrandByIds(brandIds);
        return R.ok().put("brands", brands);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("product:brand:save")
    public R save(@Validated(AddGroup.class) @RequestBody BrandEntity brand){
		brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:brand:update")
    public R update(@Validated(UpdateGroup.class) @RequestBody BrandEntity brand){
		brandService.updateDetailById(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update/status")
//    @RequiresPermissions("product:brand:update")
    public R updateCategoryShowStatus(@RequestBody BrandEntity brand){
        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds){
		brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
