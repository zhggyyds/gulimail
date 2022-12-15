package com.hao.gulimall.ware.service.impl;

import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.hao.common.utils.DateUtils;
import com.hao.gulimall.ware.entity.SeckillSkuRelationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hao.common.utils.PageUtils;
import com.hao.common.utils.Query;

import com.hao.gulimall.ware.dao.SeckillSessionDao;
import com.hao.gulimall.ware.entity.SeckillSessionEntity;
import com.hao.gulimall.ware.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {


    @Autowired
    SeckillSkuRelationServiceImpl seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
        // 计算最近三天起止时间
        String startTime = DateUtils.currentStartTime();// 当天00:00:00
        String endTime = DateUtils.getTimeByOfferset(2);// 后天23:59:59

        // 查询起止时间内的秒杀场次
        List<SeckillSessionEntity> sessions = baseMapper.selectList(new QueryWrapper<SeckillSessionEntity>()
                .between("start_time", startTime, endTime));

        // 组合秒杀关联的商品信息
        if (!CollectionUtils.isEmpty(sessions)) {
            // 组合场次ID
            List<Long> sessionIds = sessions.stream().map(SeckillSessionEntity::getId).collect(Collectors.toList());
            // 查询秒杀场次关联商品信息
            Map<Long, List<SeckillSkuRelationEntity>> skuMap = seckillSkuRelationService
                    .list(new QueryWrapper<SeckillSkuRelationEntity>().in("promotion_session_id", sessionIds))
                    .stream().collect(Collectors.groupingBy(SeckillSkuRelationEntity::getPromotionSessionId));
            sessions.forEach(session -> {
                session.setRelationSkus(skuMap.get(session.getId()));
            });
        }
        return sessions;
    }

}