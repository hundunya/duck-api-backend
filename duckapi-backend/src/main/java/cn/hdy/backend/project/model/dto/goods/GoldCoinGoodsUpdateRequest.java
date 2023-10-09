package cn.hdy.backend.project.model.dto.goods;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新请求
 *
 * @author 滴滴鸭
 */
@Data
public class GoldCoinGoodsUpdateRequest implements Serializable {
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

    private static final long serialVersionUID = 1L;
}