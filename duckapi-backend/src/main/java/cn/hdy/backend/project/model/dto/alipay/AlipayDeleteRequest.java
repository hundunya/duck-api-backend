package cn.hdy.backend.project.model.dto.alipay;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 混沌鸭
 **/
@Data
public class AlipayDeleteRequest implements Serializable {

    /**
     * 订单号
     */
    private String outTradeNo;
}
