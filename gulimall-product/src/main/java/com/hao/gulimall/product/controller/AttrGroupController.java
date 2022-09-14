package com.hao.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//import org.apache.shiro.authz.annotation.RequiresPermissions;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hao.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.hao.gulimall.product.entity.AttrEntity;
import com.hao.gulimall.product.entity.CategoryEntity;
import com.hao.gulimall.product.service.AttrAttrgroupRelationService;
import com.hao.gulimall.product.service.AttrService;
import com.hao.gulimall.product.service.CategoryService;
import com.hao.gulimall.product.vo.AttrAttrGroupVo;
import com.hao.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.hao.gulimall.product.entity.AttrGroupEntity;
import com.hao.gulimall.product.service.AttrGroupService;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.R;



/**
 * 属性分组
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService relationService;

    /**
     * 修改后，点击左侧树形三级表的某个三级目录后只显示它的属性
     */
    @RequestMapping("/list/{catelogId}")
    public R list(@RequestParam Map<String, Object> params,@PathVariable Long catelogId){
        //        PageUtils page = attrGroupService.queryPage(params);
        PageUtils page = attrGroupService.queryPage(params,catelogId);
        return R.ok().put("page", page);
    }


    /**
     * 信息，点击新增可以正常显示下拉框已选择的分类，下拉框需要一个完整的路径信息数组才能正常显示，所以新增属性路径数据返回给前端
     */
    @RequestMapping("/info/{attrGroupId}")
//   @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);
        attrGroup.setCatelogPath(categoryService.findCateLogPath(attrGroup.getCatelogId()));
        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//   @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup){
		;

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));
        return R.ok();
    }

    @GetMapping("/{attrGroupId}/attr/relation")
    public R getAllRelation(@PathVariable("attrGroupId") Long attrGroupId){
        List<AttrEntity> list = attrService.getRelation(attrGroupId);
        return R.ok().put("data",list);
    }

    @PostMapping("/attr/relation/delete")
    // 使用vo对象去接受前端的对象
    public R delete(@RequestBody AttrAttrGroupVo[] attrAttrGroupVos){
        attrGroupService.deleteRelation(attrAttrGroupVos);
        return R.ok();
    }

    @GetMapping("/{attrGroupId}/noattr/relation")
    public R getNoRelation(@RequestParam Map<String, Object> params,@PathVariable("attrGroupId")  Long attrGroupId){
        PageUtils page  = attrService.getNoRelations(params,attrGroupId);
        return R.ok().put("page",page);
    }

    //批量保存
    @PostMapping("/attr/relation")
    public R saveRelation(@RequestBody List<AttrAttrGroupVo> vos){
        List<AttrAttrgroupRelationEntity> collect = vos.stream().map(item -> {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, relationEntity);
            return relationEntity;
        }).collect(Collectors.toList());
        relationService.saveBatch(collect);

        return R.ok();
    }

    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId")Long catelogId){

        //1、查出当前分类下的所有属性分组，
        //2、查出每个属性分组的所有属性
        List<AttrGroupWithAttrsVo> vos =  attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data",vos);
    }


}
