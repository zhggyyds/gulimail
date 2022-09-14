package com.hao.gulimall.ware.Vo;

import lombok.Data;

import java.util.List;

/**
 * @author zhouhao
 * @PackageName:com.hao.gulimall.ware.Vo
 * @Description:
 * @date 2022/9/14 16:34
 **/
@Data
public class MerageVo {

    private Long purchaseId;

    private List<Long> items;
}
