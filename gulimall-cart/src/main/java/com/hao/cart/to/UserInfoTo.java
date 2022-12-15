package com.hao.cart.to;

import lombok.Data;
import lombok.ToString;

/**
 * @author zhouhao
 * @PackageName:com.hao.cart.to
 * @Description: 将拦截器处理好的数据直接给控制器使用
 * @date 2022/11/23 16:15
 **/
@ToString
@Data
public class UserInfoTo {
    private Long userId;// 用户ID，登录状态下该值非空
    private String userKey; // 临时用户UUID，非登录状态下该值非空
    private boolean firstAddCookie = false;// 判断客户端是否存在游客cookie（true：不存在，需要分配一个；false：存在，但不自动续期）
}