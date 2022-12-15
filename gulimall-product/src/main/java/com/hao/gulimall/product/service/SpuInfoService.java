package com.hao.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.utils.PageUtils;
import com.hao.gulimall.product.entity.SpuInfoEntity;
import com.hao.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 10:39:08
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveInfo(SpuSaveVo spuInfo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageInfo(Map<String, Object> params);

    void up(Long spuId);

    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

