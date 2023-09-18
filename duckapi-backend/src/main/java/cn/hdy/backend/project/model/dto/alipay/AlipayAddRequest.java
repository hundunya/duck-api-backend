package cn.hdy.backend.project.model.dto.alipay;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 混沌鸭
 **/
@Data
public class AlipayAddRequest implements Serializable {
    /**
     * 接口ID
     */
    private Long interfaceId;

    /**
     * 购买的调用次数
     */
    private Integer invokeCount;
}
