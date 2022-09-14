package com.hao.gulimall.product.dao;

import com.hao.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


/**
 * 品牌分类关联
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
@Mapper
public interface CategoryBrandRelationDao extends BaseMapper<CategoryBrandRelationEntity> {

    void updateCateLog(@Param(value = "catId") Long cateId, @Param(value = "name")String name);
}
