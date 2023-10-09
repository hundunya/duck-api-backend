package cn.hdy.backend.project.model.dto.order;

import cn.hdy.backend.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户查询请求
 *
 * @author 滴滴鸭
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GoldCoinGoodsOrderQueryRequest extends PageRequest implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 购买者ID
     */
    private Long userId;

    /**
     * 订单号
     */
    private String outTradeNo;

    /**
     * 订单名称
     */
    private String name;

    /**
     * 订单描述
     */
    private String description;

    /**
     * 金币数量
     */
    private Integer number;

    /**
     * 实际支付的交易金额
     */
    private Double payAmount;

    /**
     * 订单状态，0-待支付，1-已完成，2-取消
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}