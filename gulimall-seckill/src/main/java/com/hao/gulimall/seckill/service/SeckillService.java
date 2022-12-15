package com.hao.gulimall.seckill.service;

import com.hao.common.to.seckill.SeckillSkuRedisTO;

import java.util.List;

public interface SeckillService {

    /**
     * 上架最近三天需要秒杀的商品
     */
    void uploadSeckillSkuLatest3Days();

    List<SeckillSkuRedisTO> getCurrentSeckillSkus();

    SeckillSkuRedisTO getSkuSeckilInfo(Long skuId);

    String kill(String killId, String key, Integer num);
}
