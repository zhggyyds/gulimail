package com.hao.common.vo.cart;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.cart.vo
 * @Description:
 * @date 2022/11/23 09:34
 **/
@Data
public class CartItemVo {
    private Long skuId;                     // skuId
    private String title;                   // 标题
    private String image;                   // 图片
    private List<String> skuAttrValues;     // 销售属性
    private BigDecimal price;               // 单价
    private Integer count;                  // 商品件数
    private BigDecimal totalPrice;          // 总价

}
