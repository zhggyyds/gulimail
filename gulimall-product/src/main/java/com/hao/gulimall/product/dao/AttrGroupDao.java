package com.hao.gulimall.product.dao;

import com.hao.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hao.gulimall.product.vo.SpuItemAttrGroupVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemAttrGroupVO> getAttrGroupWithAttrs(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
