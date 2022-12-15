package com.hao.common.to.order;

import com.hao.common.to.ware.SkuWareHasStockTo;
import lombok.Data;

import java.util.List;

/**
 * 锁定库存传输对象
 * 创建订单时封装所有订单项进行锁定
 */

@Data
public class WareSkuLockTo {
    private String orderSn;
    /** 需要锁住的所有商品信息 **/
    private List<SkuWareHasStockTo> locks;
}
