package com.hao.common.to;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

// sku减钱To    包括满减信息，阶梯优惠信息，会员价
@Data
public class SkuReductionTo {

    private Long skuId;
    private int fullCount;
    private BigDecimal discount;
    private int countStatus;
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private int priceStatus;
    private List<MemberPrice> memberPrice;
}
