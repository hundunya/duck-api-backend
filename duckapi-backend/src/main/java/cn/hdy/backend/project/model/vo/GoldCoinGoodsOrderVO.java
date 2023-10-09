package cn.hdy.backend.project.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视图
 *
 * @author 滴滴鸭
 */
@Data
public class GoldCoinGoodsOrderVO implements Serializable {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 实际应支付的交易金额（元）
     */
    private Double totalAmount;

    /**
     * 实际支付的交易金额
     */
    private Double payAmount;

    /**
     * 订单状态，0-待支付，1-已完成，2-取消
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}