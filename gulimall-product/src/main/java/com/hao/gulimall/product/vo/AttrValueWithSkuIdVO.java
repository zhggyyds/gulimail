package com.hao.gulimall.product.vo;

import lombok.Data;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.vo
 * @Description:
 * @date 2022/11/9 16:39
 **/
@Data
public class AttrValueWithSkuIdVO {
    private String attrValue;// 参数值
    private String skuIds; // 这些参数值对应的skuId
}

