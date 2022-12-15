package com.hao.common.vo.ware;

import lombok.Data;

/**
 * @author zhouhao
 * @PackageName:com.hao.common.to
 * @Description:
 * @date 2022/9/19 12:48
 **/
@Data
public class SkuHasStockVo {
    private Long SkuId;
    private Boolean hasStock;
}
