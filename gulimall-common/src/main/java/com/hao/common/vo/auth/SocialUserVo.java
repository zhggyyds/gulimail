package com.hao.common.vo.auth;

import lombok.Data;

/**
 * @author zhouhao
 * @PackageName:com.hao.common.vo.auth
 * @Description:  通过code换来的令牌等数据
 * @date 2022/11/16 19:41
 **/

@Data
public class SocialUserVo {
    private String accessToken;
    private long expiresIn;
    private String uid;
    private String isRealName;
}
