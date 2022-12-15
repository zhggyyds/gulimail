package com.hao.common.vo.ware;

import com.hao.common.vo.member.MemberAddressVo;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author zhouhao
 * @PackageName:com.hao.common.vo.ware
 * @Description:
 * @date 2022/12/1 20:26
 **/

@Data
public class FareVo {
    private BigDecimal fare;
    private MemberAddressVo address;
}
