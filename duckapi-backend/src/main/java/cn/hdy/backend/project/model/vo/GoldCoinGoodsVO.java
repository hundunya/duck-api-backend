package cn.hdy.backend.project.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商城视图（脱敏）
 *
 * @author 滴滴鸭
 */
@Data
public class GoldCoinGoodsVO implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 金币数量
     */
    private Integer number;

    /**
     * 商品价格，默认为0
     */
    private Double price;

    /**
     * 创建者
     */
    private Long createUser;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}