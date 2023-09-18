package cn.hdy.backend.project.model.dto.interfaceinfo;

import lombok.Data;

import java.io.Serializable;

/**
 * 调试接口参数请求
 *
 * @author 滴滴鸭
 */
@Data
public class InvokeInterfaceParamRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 参数
     */
    private String param;

    private static final long serialVersionUID = 1L;
}