package cn.hdy.backend.project.service.impl;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.constant.CommonConstant;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.model.dto.order.GoldCoinGoodsOrderQueryRequest;
import cn.hdy.backend.project.model.vo.GoldCoinGoodsOrderVO;
import cn.hdy.backend.project.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hdy.backend.project.model.entity.GoldCoinGoodsOrder;
import cn.hdy.backend.project.service.GoldCoinGoodsOrderService;
import cn.hdy.backend.project.mapper.GoldCoinGoodsOrderMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author 滴滴鸭
*/
@Service
public class GoldCoinGoodsOrderServiceImpl extends ServiceImpl<GoldCoinGoodsOrderMapper, GoldCoinGoodsOrder>
    implements GoldCoinGoodsOrderService{

    @Override
    public GoldCoinGoodsOrderVO getGoldCoinGoodsOrderVO(GoldCoinGoodsOrder goldCoinGoodsOrder) {
        if (goldCoinGoodsOrder == null) {
            return null;
        }
        GoldCoinGoodsOrderVO goldCoinGoodsOrderVO = new GoldCoinGoodsOrderVO();
        BeanUtils.copyProperties(goldCoinGoodsOrder, goldCoinGoodsOrderVO);
        return goldCoinGoodsOrderVO;
    }

    @Override
    public List<GoldCoinGoodsOrderVO> getGoldCoinGoodsOrderVO(List<GoldCoinGoodsOrder> goldCoinGoodsOrderList) {
        if (CollectionUtils.isEmpty(goldCoinGoodsOrderList)) {
            return new ArrayList<>();
        }
        return goldCoinGoodsOrderList.stream().map(this::getGoldCoinGoodsOrderVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<GoldCoinGoodsOrder> getQueryWrapper(GoldCoinGoodsOrderQueryRequest goldCoinGoodsOrderQueryRequest) {
        if (goldCoinGoodsOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = goldCoinGoodsOrderQueryRequest.getId();
        String outTradeNo = goldCoinGoodsOrderQueryRequest.getOutTradeNo();
        String name = goldCoinGoodsOrderQueryRequest.getName();
        String description = goldCoinGoodsOrderQueryRequest.getDescription();
        Integer number = goldCoinGoodsOrderQueryRequest.getNumber();
        Double payAmount = goldCoinGoodsOrderQueryRequest.getPayAmount();
        Integer status = goldCoinGoodsOrderQueryRequest.getStatus();
        String sortField = goldCoinGoodsOrderQueryRequest.getSortField();
        String sortOrder = goldCoinGoodsOrderQueryRequest.getSortOrder();

        QueryWrapper<GoldCoinGoodsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(outTradeNo), "out_trade_no", outTradeNo);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.eq(number != null, "number", number);
        queryWrapper.eq(payAmount != null, "pay_amount", payAmount);
        queryWrapper.eq(status != null, "status", status);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }
}




