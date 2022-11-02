package com.hao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.utils.PageUtils;
import com.hao.gulimall.product.entity.CategoryEntity;
import com.hao.gulimall.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> RemovedList);

    Long[] findCateLogPath(Long catelogId);

    void updateCascadeById(CategoryEntity category);

    List<CategoryEntity> getLevel1Categorys();

    Map<String, List<Catelog2Vo>> getCatelogJson() throws InterruptedException;
}

