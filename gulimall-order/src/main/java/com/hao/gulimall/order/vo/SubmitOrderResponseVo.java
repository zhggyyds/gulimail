package com.hao.gulimall.order.vo;

import com.hao.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author zhouhao
 * @PackageName:com.hao.common.vo.order
 * @Description: 提交订单返回结果
 * @date 2022/12/3 12:02
 **/

@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;
    private Integer code;
}
