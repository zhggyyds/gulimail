package com.hao.gulimall.order.Alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hao.common.vo.order.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;


@ConfigurationProperties(prefix = "alipay") // 绑定配置，可使用元数据配置
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "2021000121698913";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCHnjwYJqGQjVm2HpBza8OkezuWIeBgjSkpx+f80wzGWAJ5pQGhD2xFPXRBprxtq7TnGj1zFJXeurL67F9S68Ui7VKuEZm5jqlEjTlLFmgPGSZGKQ3GWHtwYnBG8Xw2O4FGg2dFTO5ynLnkcu1XUf4Umny1vqMA+Of4mKmLDsu1qbXouvY1R8Bie66UHjosmXbXXSFPE7tqxkfRfNiG8foseRKc2yRVyukYjngLSMbGT5UhzoEi5WcYnHOlgZvMEK0tyXxihHJ9h71DcOajJWaLg3mMV+YaN3aNihdLPfIVlorgMZi8cq5yLN7ExgA5aRdO+xeTZJKa2DyeXfTu5OBLAgMBAAECggEAJkhgpmChUvDT1jkihbJx88hltaeycw7mA8lOj2v0Ozk02anen9x+r7z9SFsM7dh+pI4fspCpDcqeI37+GaY8a/OaV1DYmCzRU2yaM7wLe9eZmkyUet0XW04ua5hXe+eoQoNtb36cAPaE8xwOK4wVsTM66QL7eJbKxL4zffGbR5hLGuBH3fv7Jqlyr5QJ0+S4FfLcVC3GweMjdmSl9U6Gnz0chpFzf49K4A1iDFw6y5gVICgPbxKKeTgneDp1+GwkiYug7cVQTjaGC8pZHq+Nkw8Xd7DEznLYMmecUOcgS25gXW2NBi8JQE99vWCgH1UixaOasNWKsD2sS3TmMkYOQQKBgQDKC17SuXp7g4eYX0cl7MQL3cc+sjgZyBKaiEZdxaVbEaI+SRRlmAFuPPbHI/R7nfzWgBXWBZBcDPyPYildq/xP7XbbmBTJnPXBL/jinxaPcL6S7VueWJ5wXkqdcaku0zCcjvTFTLcAiD8FLBKqqHQaCePdJsrECTAmeNmY+7Q5MwKBgQCr1ayxNQJyKsbKaoxyLnkz6zVuf7bIOFh+moW833WG8n3iDw7W3E1nYhe+t6Pmh/xi93G2sexaM38dNt0qi9/u6aPnrS3791X6799YFIsZtiGUVJcJKilJ3KqZOh8f4dQVO2cUlrMbkugQqLg9YNbZXSK8msK32ann23C97uysiQKBgQCt0gZsCAIlzMyU/DM7n2wyQUSu3aiCFaxboi3XcriFlrAcIccYeCOS8YPlYztsNklVSLCAhhzh9JI70NmBqvrQ0JzW4wnPQefzog/e97rzAK4TvXx6yAbW6WgL98Lzc+F4b0yJiSxPmueMzZPpmi74T9XnZv4+lSEFmCasBK/SHQKBgHvKQk+jPbDbqjx2h9lWK5DAmMyTCy2wkegbD8+iN/vQw+lQKcOWD9kzrKGDkHrqamMtmekgwoG06ZU1Sh11iQyHmsGzXBZR98oAdqjwm8kZEZXXXruGeRox82DRo0yHlw74rWkCq+NocPBXa+sj0YPj+btfznk+QI2847qg3jXpAoGADtcylbiYgaSmk6/yQFs/OkCub9I7medjQd6U9Vf1C+f3yyuSPcU+EMZV2SFStmyu+91Fqv0hDio8QDwLxrf8acd+SguaanzpRUq91ftHRqdiLkFvqAQIFiFWU33/GPzejb0eSR6rHSt/E3EZWxOyylHIvTopOF4/4Aoy6V7Us5c=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiVPa2ox8zpExCFuHuOqB1OVHkSwjN1H9JzsmdXOo3oxXKhC+mZCBoaTrbBg42g3E8pMclMqV1SkPnOt+TQLYg2NHGvRTUTu7TImA43ZOrdLuvPxM9N3hJBtzZfBhTmSQuVLWFEf8x8lnqRhP08y67jsPp7xDygE0KysqI46xlvGaxrZdW3Sunfe8u20C5udUv2lKD/BOC/IjkTUFhJRHHP80/b6bzvYjoiKu6OlzPKn+N4A6JTCcz2bJUGnyPsh09GghpSO18K5mVY552dVdMwqFKOZ61c1PL5ob69gLGvwSsYXNggN2cpLIvWak9V+zg5l03QGv7an6ugSUjI19swIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url = "https://489l88001p.zicp.fun/pay/notify";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 同步通知，支付成功，跳转到成功页
    private  String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }

    /**
     * 验签
     *
     * @param request 回参
     */
    public Boolean verify(HttpServletRequest request) throws AlipayApiException {
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (String name : requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        return AlipaySignature.rsaCheckV1(params, this.getAlipay_public_key(),
                this.getCharset(), this.getSign_type()); //调用SDK验证签名
    }
}
