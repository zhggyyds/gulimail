package com.hao.gulimall.product.dao;

import com.hao.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:08
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void upSpuStatus(@Param(value = "spuId") Long spuId, @Param(value = "code") int code);
}
