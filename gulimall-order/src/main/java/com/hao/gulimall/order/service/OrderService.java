package com.hao.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hao.common.to.mq.SeckillOrderTO;
import com.hao.common.utils.PageUtils;
import com.hao.common.vo.order.OrderConfirmVo;
import com.hao.common.vo.order.OrderSubmitVo;
import com.hao.common.vo.order.PayVo;
import com.hao.gulimall.order.Alipay.vo.PayAsyncVo;
import com.hao.gulimall.order.entity.OrderEntity;
import com.hao.gulimall.order.vo.SubmitOrderResponseVo;

import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * 订单
 *
 * @author zh
 * @email zh@gmail.com
 * @date 2022-07-28 11:50:23
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo getOrderConfirmData() throws ExecutionException, InterruptedException;

    SubmitOrderResponseVo submitOrder(OrderSubmitVo orderSubmitVo) throws Exception;

    OrderEntity getOrderByOrderSn(String orderSn);

    void closeOrder(OrderEntity orderEntity);

    /*
     * @description 封装支付宝需要的数据
     * @date 2022/12/9 22:00
     * @param null
     * @return null
     */
    PayVo getPayVo(String orderSn);

    /*
     * @description 获得携带商品信息的订单数据
     * @date 2022/12/10 14:25
     * @param null
     * @return null
     */
    PageUtils queryPageWithItems(Map<String, Object> params);

    /*
     * @description 处理支付宝异步通知
     * @date 2022/12/10 21:13
     * @param null
     * @return null
     */
    void handlePayResult(PayAsyncVo aliVo);

    /*
     * @description 创建秒杀订单
     * @date 2022/12/12 21:36
     * @param null
     * @return null
     */
    void createSeckillOrder(SeckillOrderTO order);
}

