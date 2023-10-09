package cn.hdy.backend.project.service;

import cn.hdy.backend.project.model.dto.goods.GoldCoinGoodsQueryRequest;
import cn.hdy.backend.project.model.entity.GoldCoinGoods;
import cn.hdy.backend.project.model.vo.GoldCoinGoodsVO;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 滴滴鸭
*/
public interface GoldCoinGoodsService extends IService<GoldCoinGoods> {

    GoldCoinGoodsVO getGoldCoinGoodsVO(GoldCoinGoods goldCoinGoods);

    List<GoldCoinGoodsVO> getGoldCoinGoodsVO(List<GoldCoinGoods> goldCoinGoodsList);

    QueryWrapper<GoldCoinGoods> getQueryWrapper(GoldCoinGoodsQueryRequest goldCoinGoodsQueryRequest);

    void validGoldCoinGoods(GoldCoinGoods goldCoinGoods, boolean add);
}
