package cn.hdy.backend.project.service.impl;

import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.constant.CommonConstant;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.exception.ThrowUtils;
import cn.hdy.backend.project.mapper.GoldCoinGoodsMapper;
import cn.hdy.backend.project.model.dto.goods.GoldCoinGoodsQueryRequest;
import cn.hdy.backend.project.model.entity.GoldCoinGoods;
import cn.hdy.backend.project.model.vo.GoldCoinGoodsVO;
import cn.hdy.backend.project.service.GoldCoinGoodsService;
import cn.hdy.backend.project.utils.SqlUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author 滴滴鸭
*/
@Service
public class GoldCoinGoodsServiceImpl extends ServiceImpl<GoldCoinGoodsMapper, GoldCoinGoods>
    implements GoldCoinGoodsService {

    @Override
    public GoldCoinGoodsVO getGoldCoinGoodsVO(GoldCoinGoods goldCoinGoods) {
        if (goldCoinGoods == null) {
            return null;
        }
        GoldCoinGoodsVO goldCoinGoodsVO = new GoldCoinGoodsVO();
        BeanUtils.copyProperties(goldCoinGoods, goldCoinGoodsVO);
        return goldCoinGoodsVO;
    }

    @Override
    public List<GoldCoinGoodsVO> getGoldCoinGoodsVO(List<GoldCoinGoods> goldCoinGoodsList) {
        if (CollectionUtils.isEmpty(goldCoinGoodsList)) {
            return new ArrayList<>();
        }
        return goldCoinGoodsList.stream().map(this::getGoldCoinGoodsVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<GoldCoinGoods> getQueryWrapper(GoldCoinGoodsQueryRequest goldCoinGoodsQueryRequest) {
        if (goldCoinGoodsQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = goldCoinGoodsQueryRequest.getId();
        String name = goldCoinGoodsQueryRequest.getName();
        String description = goldCoinGoodsQueryRequest.getDescription();
        Integer number = goldCoinGoodsQueryRequest.getNumber();
        Double price = goldCoinGoodsQueryRequest.getPrice();
        Long createUser = goldCoinGoodsQueryRequest.getCreateUser();
        String sortField = goldCoinGoodsQueryRequest.getSortField();
        String sortOrder = goldCoinGoodsQueryRequest.getSortOrder();
        QueryWrapper<GoldCoinGoods> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.like(StringUtils.isNotBlank(description), "description", description);
        queryWrapper.eq(number != null, "number", number);
        queryWrapper.eq(price != null, "price", price);
        queryWrapper.eq(createUser != null, "create_user", createUser);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void validGoldCoinGoods(GoldCoinGoods goldCoinGoods, boolean add) {
        if (goldCoinGoods == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String name = goldCoinGoods.getName();
        String description = goldCoinGoods.getDescription();
        Integer number = goldCoinGoods.getNumber();
        Double price = goldCoinGoods.getPrice();

        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(name, description), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(number == null, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(price == null, ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(name) && name.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品名称过长");
        }
        if (StringUtils.isNotBlank(description) && description.length() > 256) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品描述过长");
        }
        if (number <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "金币数量必须大于0");
        }
        if (price <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "商品单价必须大于0");
        }
    }
}




