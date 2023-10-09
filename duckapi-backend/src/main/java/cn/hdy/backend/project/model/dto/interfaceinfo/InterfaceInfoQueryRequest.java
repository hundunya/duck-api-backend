package cn.hdy.backend.project.model.dto.interfaceinfo;

import cn.hdy.backend.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 查询请求
 *
 * @author 滴滴鸭
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoQueryRequest extends PageRequest implements Serializable {

    /**
     * 主键
     */
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 请求类型
     */
    private String method;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 请求参数
     */
    private String requestParam;

    /**
     * 响应参数
     */
    private String responseParam;

    /**
     * 接口调用一次的单价
     */
    private Integer price;

    /**
     * 接口状态(0-关闭，1-关闭)
     */
    private Integer status;

    /**
     * 接口创建者ID
     */
    private Long createUser;

    private static final long serialVersionUID = 1L;
}