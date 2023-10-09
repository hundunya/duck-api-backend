package cn.hdy.backend.project.model.dto.goods;

import cn.hdy.backend.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户查询请求
 *
 * @author 滴滴鸭
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class GoldCoinGoodsQueryRequest extends PageRequest implements Serializable {
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

    private static final long serialVersionUID = 1L;
}