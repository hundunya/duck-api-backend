package cn.hdy.backend.project.service;

import cn.hdy.backend.project.model.dto.order.GoldCoinGoodsOrderQueryRequest;
import cn.hdy.backend.project.model.entity.GoldCoinGoodsOrder;
import cn.hdy.backend.project.model.vo.GoldCoinGoodsOrderVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 滴滴鸭
*/
public interface GoldCoinGoodsOrderService extends IService<GoldCoinGoodsOrder> {

    GoldCoinGoodsOrderVO getGoldCoinGoodsOrderVO(GoldCoinGoodsOrder goldCoinGoodsOrder);

    List<GoldCoinGoodsOrderVO> getGoldCoinGoodsOrderVO(List<GoldCoinGoodsOrder> goldCoinGoodsOrderList);

    QueryWrapper<GoldCoinGoodsOrder> getQueryWrapper(GoldCoinGoodsOrderQueryRequest goldCoinGoodsOrderQueryRequest);
}
