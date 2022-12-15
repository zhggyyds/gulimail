package com.hao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.utils.PageUtils;
import com.hao.gulimall.product.entity.SkuImagesEntity;
import com.hao.gulimall.product.entity.SkuInfoEntity;
import com.hao.gulimall.product.vo.SkuItemVO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * sku信息
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:07
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuInfo(SkuInfoEntity skuInfoEntity);

    PageUtils queryPageInfo(Map<String, Object> params);

    List<SkuInfoEntity> getSkusBySpuId(Long spuId);

    /*
     * @description 获得商品详情页的信息
     * @date 2022/12/12 16:20
     * @param null
     * @return null
     */
    SkuItemVO itemInfo(Long skuId) throws ExecutionException, InterruptedException;

    List<SkuInfoEntity> getByIds(List<Long> skuIds);
}

