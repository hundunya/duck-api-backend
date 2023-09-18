package cn.hdy.backend.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 接口调用次数购买记录订单
 *
 * @author 滴滴鸭
 */
@TableName(value = "invoke_count_order")
@Data
public class InvokeCountOrder implements Serializable {

    /**
    * 主键
    */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
    * 订单号
    */
    private String outTradeNo;

    /**
    * 实际应支付的交易金额
    */
    private Double totalAmount;

    /**
    * 实际支付的金额
    */
    private Double payAmount;

    /**
    * 购买接口ID
    */
    private Long interfaceId;

    /**
    * 用户ID
    */
    private Long userId;

    /**
    * 购买的接口调用次数
    */
    private Integer invokeCount;

    /**
    * 交易状态，0-待支付，1-已完成，2-取消
    */
    private Integer status;

    /**
    * 创建时间
    */
    private Date createTime;

    /**
    * 更新时间
    */
    private Date updateTime;

    /**
    * 逻辑删除，0-正常，1-删除
    */
    @TableLogic
    private Integer isDelete;
}
