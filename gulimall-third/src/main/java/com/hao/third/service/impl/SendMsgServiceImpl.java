package com.hao.third.service.impl;


import lombok.Data;
import org.apache.http.HttpResponse;
import com.hao.third.service.SendMsgService;
import com.hao.third.util.HttpUtils;
import org.apache.http.util.EntityUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @PackageName:com.hao.third.service.impl
 * @Description:
 * @date 2022/11/11 15:32
 **/
@Data
@ConfigurationProperties(prefix = "spring.cloud.alicloud.sms")
@Service
public class SendMsgServiceImpl implements SendMsgService {

    private String host;
    private String path;
    private String method;
    private String appcode;

    @Override
    public void sendMsg(String phone, String code) {

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Authorization", "APPCODE " + appcode);
            Map<String, String> querys = new HashMap<String, String>();
            querys.put("mobile", phone);//手机号
            querys.put("param", "**code**:"+code); // 验证码
            querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
            querys.put("templateId", "305b8db49cdb4b18964ffe255188cb20");
            Map<String, String> bodys = new HashMap<String, String>();


            try {
                HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
                System.out.println(response.toString());
                //获取response的body
                System.out.println(EntityUtils.toString(response.getEntity()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
}
