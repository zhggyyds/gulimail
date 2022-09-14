package com.hao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.utils.PageUtils;
import com.hao.gulimall.product.entity.SkuInfoEntity;

import java.util.Map;

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
}

