package cn.hdy.backend.project.utils;

import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.exception.BusinessException;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 混沌鸭
 **/
@Slf4j
@Component
public class AlipayUtils {
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

    private AlipayClient alipayClient;

    /**
     * 创建订单
     * @param alipayTradePrecreateRequest 封装的订单请求
     * @return 订单创建结果
     */
    public AlipayTradePrecreateResponse execute(AlipayTradePrecreateRequest alipayTradePrecreateRequest){
        if (alipayClient == null){
            alipayClient = new DefaultAlipayClient(gatewayUrl, appId, privateKey, FORMAT, charset, publicKey, signType);
        }
        AlipayTradePrecreateResponse response;
        try {
            response = alipayClient.execute(alipayTradePrecreateRequest);
        } catch (AlipayApiException e) {
            log.info("下单失败 错误代码:{}, 错误信息:{}", e.getErrCode(), e.getErrMsg());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "下单失败！");
        }
        log.info("AlipayTradePrecreateResponse = {}", response.getBody());

        if (!response.isSuccess()) {
            log.info("下单失败 错误代码:{}, 错误信息:{}", response.getCode(), response.getMsg());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "下单失败！");
        }
        return response;
    }

    /**
     * 签名验证
     * @param parameterMap 支付成功后异步通知参数
     * @return 验证结果
     */
    public boolean verifySign(Map<String, String[]> parameterMap){
        //获取支付宝POST过来反馈信息，将异步通知中收到的待验证所有参数都存放到map中
        Map<String, String> params = new HashMap<>();
        for (String name : parameterMap.keySet()) {
            String[] values = parameterMap.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            params.put(name, valueStr);
        }
        //调用SDK验证签名
        //公钥验签示例代码
        boolean signVerified;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, publicKey, charset, signType);
        } catch (AlipayApiException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return signVerified;
    }
}
