package com.hao.common.to.ware;

import lombok.Data;

import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.common.to.ware
 * @Description: 用于查询那些仓库有相关商品库存的对象封装
 * @date 2022/12/4 17:54
 **/
@Data
public class SkuWareHasStockTo{
    private Long skuId;
    private List<Long> wareId;
    private Integer count;
}
