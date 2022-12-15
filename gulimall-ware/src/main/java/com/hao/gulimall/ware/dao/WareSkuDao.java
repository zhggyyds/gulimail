package com.hao.gulimall.ware.dao;

import com.hao.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 13:08:54
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void addStock(Long skuId, Long wareId, Integer skuNum);

     Long getSkuHasStocks(Long skuId);

    List<Long> listWareId(@Param("skuId") Long skuId);

    Long lockSkuStock(@Param("wareId") Long wareId, @Param("skuId") Long skuId, @Param("count") Integer count);

    /**
     * 解锁库存
     * @param skuId
     * @param wareId
     * @param count
     */
    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("count") Integer count);
}
