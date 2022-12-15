package com.hao.common.to.order;

import lombok.Data;

/**
 * @author zhouhao
 * @PackageName:com.hao.common.to.order
 * @Description: 锁定库存的响应结果
 * @date 2022/12/4 16:19
 **/
@Data
public class SkuStockResponseTo {

    private Long skuId;

    private Boolean hasStock;
}
