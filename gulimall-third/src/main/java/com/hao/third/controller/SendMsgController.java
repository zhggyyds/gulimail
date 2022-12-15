package com.hao.third.controller;

import com.hao.common.utils.R;
import com.hao.third.service.SendMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhouhao
 * @PackageName:com.hao.third.controller
 * @Description:
 * @date 2022/11/11 15:55
 **/

@RestController
@RequestMapping("/sms")
public class SendMsgController {
    @Autowired
    SendMsgService sendMsgService;

    /**
     * 发送短信验证码
     * 提供其他模块调用
     * @param phone 号码
     * @param code  验证码
     */
    @GetMapping("/sendcode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        sendMsgService.sendMsg(phone,code);
        return R.ok();
    }
}
