package com.hao.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.to.SkuHasStockVo;
import com.hao.common.utils.PageUtils;
import com.hao.gulimall.ware.entity.WareSkuEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 13:08:54
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVo> getSkuHasStocks(List<Long> skuIds);
}

