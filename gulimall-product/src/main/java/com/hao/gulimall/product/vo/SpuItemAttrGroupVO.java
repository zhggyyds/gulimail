package com.hao.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.vo
 * @Description:
 * @date 2022/11/9 16:40
 **/
@Data
@ToString
public class SpuItemAttrGroupVO {
    private String groupName;
    private List<Attr> attrs;
}
