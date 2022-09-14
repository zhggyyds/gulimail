package com.hao.gulimall.product.vo;

import com.hao.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.product.vo
 * @Description:
 * @date 2022/9/6 12:57
 **/
@Data
public class AttrGroupWithAttrsVo {
    /**
     * 分组id
     */
    private Long attrGroupId;
    /**
     * 组名
     */
    private String attrGroupName;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 描述
     */
    private String descript;
    /**
     * 组图标
     */
    private String icon;
    /**
     * 所属分类id
     */
    private Long catelogId;

    private List<AttrEntity> attrs;
}
