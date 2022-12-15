package com.hao.gulimall.product.app;

import java.util.Arrays;
import java.util.Map;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.hao.gulimall.product.vo.SpuSaveVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hao.gulimall.product.entity.SpuInfoEntity;
import com.hao.gulimall.product.service.SpuInfoService;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.R;



/**
 * spu信息
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:08
 */
@RestController
@RequestMapping("product/spuinfo")
public class SpuInfoController {
    @Autowired
    private SpuInfoService spuInfoService;

    @GetMapping(value = "/skuId/{skuId}")
    R getSpuInfoBySkuId(@PathVariable("skuId") Long skuId){
        SpuInfoEntity spuInfo = spuInfoService.getSpuInfoBySkuId(skuId);
        return R.ok().setData(spuInfo);
    }

    @PostMapping("/{spuId}/up")
    public R spuUp(@PathVariable("spuId") Long spuId){
        spuInfoService.up(spuId);
        return R.ok();
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    // Map 接受get请求中所有的参数,在service中get取出
    public R list(@RequestParam Map<String, Object> params){
        PageUtils page = spuInfoService.queryPageInfo(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//   @RequiresPermissions("product:spuinfo:info")
    public R info(@PathVariable("id") Long id){
		SpuInfoEntity spuInfo = spuInfoService.getById(id);

        return R.ok().put("spuInfo", spuInfo);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("product:spuinfo:save")
    public R save(@RequestBody SpuSaveVo spuInfo){
		spuInfoService.saveInfo(spuInfo);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:spuinfo:update")
    public R update(@RequestBody SpuInfoEntity spuInfo){
		spuInfoService.updateById(spuInfo);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:spuinfo:delete")
    public R delete(@RequestBody Long[] ids){
		spuInfoService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
