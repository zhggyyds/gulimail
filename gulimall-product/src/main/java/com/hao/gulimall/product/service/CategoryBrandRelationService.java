package com.hao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.utils.PageUtils;
import com.hao.gulimall.product.entity.BrandEntity;
import com.hao.gulimall.product.entity.CategoryBrandRelationEntity;
import com.hao.gulimall.product.service.impl.BrandServiceImpl;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveDetail(CategoryBrandRelationEntity categoryBrandRelation);

    void updateBrand(Long Id,String name);

    void updateCateLog(Long catId, String name);

    List<BrandEntity> getRelatedBrands(Long catelogId);
}

