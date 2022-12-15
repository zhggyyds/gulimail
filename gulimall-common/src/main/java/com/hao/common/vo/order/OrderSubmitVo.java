package com.hao.common.vo.order;

import lombok.Data;
import java.math.BigDecimal;

/**
 * @Description: 封装订单提交数据的vo，
 * @author: zhouhao
 */

@Data
public class OrderSubmitVo {

    //无需提交要购买的商品，提交订单时会实时查询最新的购物车商品选中数据提交,如果用户在提交订单页面时仍在购物车中添加了新商品后，点击提交订单时仍然保持最新状态

    //用户相关的信息，直接去session中取出即可

    /** 收获地址的id **/
    private String addrId;

    /** 支付方式 **/
    private Integer payType;

    // 优惠、发票

    /** 防重令牌 **/
    private String uniqueToken;

    /** 应付价格 **/
    private BigDecimal payPrice;// 页面提交应付价格，提交订单会再算一遍价格，如果价格不相等提示用户

    /** 订单备注 **/
    private String remarks;
}
