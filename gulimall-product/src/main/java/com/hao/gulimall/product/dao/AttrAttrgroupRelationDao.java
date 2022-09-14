package com.hao.gulimall.product.dao;

import com.hao.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性&属性分组关联
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
@Mapper
public interface AttrAttrgroupRelationDao extends BaseMapper<AttrAttrgroupRelationEntity> {

    void deleteBatch(@Param(value = "relationEntities") List<AttrAttrgroupRelationEntity> relationEntities);
}
