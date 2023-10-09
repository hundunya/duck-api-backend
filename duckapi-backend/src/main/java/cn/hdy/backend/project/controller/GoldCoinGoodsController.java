package cn.hdy.backend.project.controller;

import cn.hdy.backend.project.annotation.AuthCheck;
import cn.hdy.backend.project.common.BaseResponse;
import cn.hdy.backend.project.common.DeleteRequest;
import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.common.ResultUtils;
import cn.hdy.backend.project.constant.UserConstant;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.exception.ThrowUtils;
import cn.hdy.backend.project.model.dto.goods.GoldCoinGoodsAddRequest;
import cn.hdy.backend.project.model.dto.goods.GoldCoinGoodsQueryRequest;
import cn.hdy.backend.project.model.dto.goods.GoldCoinGoodsUpdateRequest;
import cn.hdy.backend.project.model.entity.GoldCoinGoods;
import cn.hdy.backend.project.model.vo.GoldCoinGoodsVO;
import cn.hdy.backend.project.service.GoldCoinGoodsService;
import cn.hdy.backend.project.service.UserService;
import cn.hdy.common.project.model.entity.User;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 金币商品接口
 *
 * @author 滴滴鸭
 */
@RestController
@RequestMapping("/mall")
@Slf4j
public class GoldCoinGoodsController {

    @Resource
    private GoldCoinGoodsService goldCoinGoodsService;
    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 添加金币商品
     *
     * @param goldCoinGoodsAddRequest 金币商品信息
     * @return 添加成功的金币商品ID
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addGoldCoinGoods(@RequestBody GoldCoinGoodsAddRequest goldCoinGoodsAddRequest, HttpServletRequest request) {
        if (goldCoinGoodsAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        GoldCoinGoods goldCoinGoods = new GoldCoinGoods();
        BeanUtils.copyProperties(goldCoinGoodsAddRequest, goldCoinGoods);
        goldCoinGoodsService.validGoldCoinGoods(goldCoinGoods, true);
        User loginUser = userService.getLoginUser(request);
        goldCoinGoods.setCreateUser(loginUser.getId());
        boolean result = goldCoinGoodsService.save(goldCoinGoods);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(goldCoinGoods.getId());
    }

    /**
     * 删除金币商品
     *
     * @param deleteRequest 金币商品ID
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteGoldCoinGoods(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = goldCoinGoodsService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 批量删除
     *
     * @param ids id数组
     * @return 删除是否成功
     */
    @PostMapping("/batch/delete")
    public BaseResponse<Boolean> deleteGoldCoinGoodsByIds(@RequestBody List<Long> ids, HttpServletRequest request) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        for (Long id : ids) {
            ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR);
            // 判断是否存在
            GoldCoinGoods goldCoinGoods = goldCoinGoodsService.getById(id);
            ThrowUtils.throwIf(goldCoinGoods == null, ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = goldCoinGoodsService.removeBatchByIds(ids);
        return ResultUtils.success(b);
    }

    /**
     * 更新金币商品
     *
     * @param goldCoinGoodsUpdateRequest 待更新金币商品信息
     * @return 更新是否成功
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateGoldCoinGoods(@RequestBody GoldCoinGoodsUpdateRequest goldCoinGoodsUpdateRequest) {
        if (goldCoinGoodsUpdateRequest == null || goldCoinGoodsUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        GoldCoinGoods goldCoinGoods = new GoldCoinGoods();
        BeanUtils.copyProperties(goldCoinGoodsUpdateRequest, goldCoinGoods);
        boolean result = goldCoinGoodsService.updateById(goldCoinGoods);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取金币商品（仅管理员）
     *
     * @param id 金币商品ID
     * @return 金币商品信息
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<GoldCoinGoods> getGoldCoinGoodsById(long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        GoldCoinGoods goldCoinGoods = goldCoinGoodsService.getById(id);
        ThrowUtils.throwIf(goldCoinGoods == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(goldCoinGoods);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id 金币商品ID
     * @return 金币商品信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<GoldCoinGoodsVO> getGoldCoinGoodsVoById(long id) {
        BaseResponse<GoldCoinGoods> response = getGoldCoinGoodsById(id);
        GoldCoinGoods goldCoinGoods = response.getData();
        return ResultUtils.success(goldCoinGoodsService.getGoldCoinGoodsVO(goldCoinGoods));
    }

    /**
     * 分页获取金币商品列表（仅管理员）
     *
     * @param goldCoinGoodsQueryRequest 金币商品查询条件
     * @return 金币商品列表
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<GoldCoinGoods>> listGoldCoinGoodsByPage(@RequestBody GoldCoinGoodsQueryRequest goldCoinGoodsQueryRequest) {
        long current = goldCoinGoodsQueryRequest.getCurrent();
        long size = goldCoinGoodsQueryRequest.getPageSize();
        Page<GoldCoinGoods> goldCoinGoodsPage = goldCoinGoodsService.page(new Page<>(current, size),
                goldCoinGoodsService.getQueryWrapper(goldCoinGoodsQueryRequest));
        return ResultUtils.success(goldCoinGoodsPage);
    }

    /**
     * 分页获取金币商品封装列表
     *
     * @param goldCoinGoodsQueryRequest 金币商品查询条件
     * @return 金币商品列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GoldCoinGoodsVO>> listGoldCoinGoodsVoByPage(@RequestBody GoldCoinGoodsQueryRequest goldCoinGoodsQueryRequest) {
        if (goldCoinGoodsQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = goldCoinGoodsQueryRequest.getCurrent();
        long size = goldCoinGoodsQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<GoldCoinGoods> goldCoinGoodsPage = goldCoinGoodsService.page(new Page<>(current, size),
                goldCoinGoodsService.getQueryWrapper(goldCoinGoodsQueryRequest));
        Page<GoldCoinGoodsVO> goldCoinGoodsVoPage = new Page<>(current, size, goldCoinGoodsPage.getTotal());
        List<GoldCoinGoodsVO> goldCoinGoodsVO = goldCoinGoodsService.getGoldCoinGoodsVO(goldCoinGoodsPage.getRecords());
        goldCoinGoodsVoPage.setRecords(goldCoinGoodsVO);
        return ResultUtils.success(goldCoinGoodsVoPage);
    }

    // endregion
}
