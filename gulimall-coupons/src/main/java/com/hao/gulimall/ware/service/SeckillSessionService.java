package com.hao.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.utils.PageUtils;
import com.hao.gulimall.ware.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-08-07 12:38:46
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SeckillSessionEntity> getLates3DaySession();
}

