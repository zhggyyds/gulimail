package com.hao.gulimall.order.dao;

import com.hao.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单
 * 
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 11:50:23
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    /*
     * @description 更新订单状态
     * @date 2022/12/10 21:19
     * @param null
     * @return null
     */
    void updateOrderStatus(@Param("code") Integer tradeStatus, @Param("orderSn") String out_trade_no);

}
