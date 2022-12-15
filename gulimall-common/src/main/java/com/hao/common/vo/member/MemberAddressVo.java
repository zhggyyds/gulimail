package com.hao.common.vo.member;
import lombok.Data;


/**
 * @author zhouhao
 * @PackageName:com.hao.common.vo.order
 * @Description: 用户收货地址
 * @date 2022/11/29 16:44
 **/


@Data
public class MemberAddressVo {

    private Long id;
    /**
     * member_id
     */
    private Long memberId;
    /**
     * 收货人姓名
     */
    private String name;
    /**
     * 电话
     */
    private String phone;
    /**
     * 邮政编码
     */
    private String postCode;
    /**
     * 省份/直辖市
     */
    private String province;
    /**
     * 城市
     */
    private String city;
    /**
     * 区
     */
    private String region;
    /**
     * 详细地址(街道)
     */
    private String detailAddress;
    /**
     * 省市区代码
     */
    private String areacode;
    /**
     * 是否默认
     */
    private Integer defaultStatus;

}
