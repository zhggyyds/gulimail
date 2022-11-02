package com.hao.gulimall.ware.dao;

import com.hao.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

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
}
