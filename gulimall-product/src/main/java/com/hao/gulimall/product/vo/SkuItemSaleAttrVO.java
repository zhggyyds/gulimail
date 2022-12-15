package com.hao.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.vo
 * @Description:
 * @date 2022/11/9 16:39
 **/
@Data
@ToString
public class SkuItemSaleAttrVO {
    /**
     * 1.销售属性对应1个attrName
     * 2.销售属性对应n个attrValue
     * 3.n个sku包含当前销售属性（所以前端根据skuId交集 来判断当前选择的是那个skuId）
     */
    private Long attrId;
    private String attrName;
    private List<AttrValueWithSkuIdVO> attrValues;
}
