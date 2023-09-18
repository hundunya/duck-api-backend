package cn.hdy.backend.project.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 混沌鸭
 **/
@Data
//@Configuration
public class AlipayConfig {
    //获取配置文件中的配置信息
    //应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    @Value("${alipay.appId}")
    private String appId;

    //商户私钥 您的PKCS8格式RSA2私钥
    @Value("${alipay.privateKey}")
    private String privateKey;

    //支付宝公钥
    @Value("${alipay.publicKey}")
    private String publicKey;

    //签名方式
    @Value("${alipay.signType}")
    private String signType;

    //字符编码格式
    @Value("${alipay.charset}")
    private String charset;

    //支付宝网关
    @Value("${alipay.gatewayUrl}")
    private String gatewayUrl;

    private final String FORMAT = "json";

    @Bean
    public AlipayClient alipayClient(){
        return new DefaultAlipayClient(gatewayUrl, appId, privateKey, FORMAT, charset, publicKey, signType);
    }
}
