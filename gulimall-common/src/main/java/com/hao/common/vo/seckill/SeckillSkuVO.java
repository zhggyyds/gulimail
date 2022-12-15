package com.hao.common.vo.seckill;

/**
 * @author zhouhao
 * @PackageName:com.hao.common.vo.seckill
 * @Description: 与某秒杀场次相关的商品秒杀信息
 * @date 2022/12/11 17:09
 **/
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillSkuVO {

    /**
     * 活动id
     */
    private Long promotionId;
    /**
     * 活动场次id
     */
    private Long promotionSessionId;
    /**
     * 商品id
     */
    private Long skuId;
    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;
    /**
     * 秒杀总量
     */
    private Integer seckillCount;
    /**
     * 每人限购数量
     */
    private Integer seckillLimit;
    /**
     * 排序
     */
    private Integer seckillSort;

    //当前商品秒杀的开始时间
    private Long startTime;

    //当前商品秒杀的结束时间
    private Long endTime;

    //当前商品秒杀的随机码 防止bot秒杀
    private String randomCode;

}

