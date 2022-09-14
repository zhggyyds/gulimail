package com.hao.gulimall.product.vo;

import com.hao.gulimall.product.entity.AttrEntity;
import lombok.Data;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.vo
 * @Description:
 * @date 2022/9/1 17:53
 **/
@Data
public class AttrRespVo extends AttrEntity {

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;

    private Long attrGroupId;

}
