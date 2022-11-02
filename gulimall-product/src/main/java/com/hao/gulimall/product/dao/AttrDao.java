package com.hao.gulimall.product.dao;

import com.hao.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<Long> getSearchAttrIds(@Param(value = "attrIds") List<Long> attrIds);
}
