package com.hao.gulimall.ware.dao;

import com.hao.gulimall.ware.entity.CouponHistoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券领取历史记录
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-08-07 12:38:46
 */
@Mapper
public interface CouponHistoryDao extends BaseMapper<CouponHistoryEntity> {
	
}
