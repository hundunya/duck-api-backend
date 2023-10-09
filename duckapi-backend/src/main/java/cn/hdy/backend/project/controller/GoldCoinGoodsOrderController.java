package cn.hdy.backend.project.controller;

import cn.hdy.backend.project.common.BaseResponse;
import cn.hdy.backend.project.common.DeleteRequest;
import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.common.ResultUtils;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.exception.ThrowUtils;
import cn.hdy.backend.project.job.RabbitProduct;
import cn.hdy.backend.project.model.dto.order.GoldCoinGoodsOrderQueryRequest;
import cn.hdy.backend.project.model.entity.GoldCoinGoods;
import cn.hdy.backend.project.model.entity.GoldCoinGoodsOrder;
import cn.hdy.backend.project.model.vo.GoldCoinGoodsOrderVO;
import cn.hdy.backend.project.service.GoldCoinGoodsOrderService;
import cn.hdy.backend.project.service.GoldCoinGoodsService;
import cn.hdy.backend.project.service.UserService;
import cn.hdy.backend.project.utils.AlipayUtils;
import cn.hdy.backend.project.utils.OutTradeNoUtils;
import cn.hdy.common.project.model.entity.User;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 滴滴鸭
 */
@Slf4j
@RestController
@RequestMapping("/alipay")
public class GoldCoinGoodsOrderController {

    //服务器异步通知页面路径
    @Value("${alipay.notifyUrl}")
    private String notifyUrl;
    //页面跳转同步通知页面路径
    @Value("${alipay.returnUrl}")
    private String returnUrl;
    @Resource
    private RabbitProduct rabbitProduct;
    @Resource
    private UserService userService;
    @Resource
    private AlipayUtils alipayUtils;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private GoldCoinGoodsService goldCoinGoodsService;
    @Resource
    private GoldCoinGoodsOrderService goldCoinGoodsOrderService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    // region 增删改查

    /**
     * 创建订单
     *
     * @param goldCoinGoodsId 金币商品ID
     * @return 订单号、支付二维码和应支付金额
     */
    @Transactional
    @PostMapping("/create")
    public BaseResponse<JSONObject> createGoldCoinGoodsOrder(Long goldCoinGoodsId, HttpServletRequest request) {
        if (goldCoinGoodsId == null || goldCoinGoodsId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询商品信息
        GoldCoinGoods goldCoinGoods = goldCoinGoodsService.getById(goldCoinGoodsId);
        ThrowUtils.throwIf(goldCoinGoods == null, ErrorCode.NOT_FOUND_ERROR, "商品不存在或已经下架");
        User loginUser = userService.getLoginUser(request);

        JSONObject bizContent = new JSONObject();
        // 1.设置订单信息
        // 订单号生成
        String outTradeNo = OutTradeNoUtils.getOutTradeNo();
        bizContent.put("out_trade_no", outTradeNo);
        Double totalAmount = goldCoinGoods.getPrice();
        bizContent.put("total_amount", totalAmount);
        // 设置订单标题
        bizContent.put("subject", "金币购买");
        // 设置商品明细信息
        JSONArray goodsDetail = new JSONArray();
        JSONObject goods = new JSONObject();
        // 商品编号
        goods.put("goods_id", "gold_coin_goods_"+System.currentTimeMillis());
        // 商品名称
        goods.put("goods_name", goldCoinGoods.getName());
        // 商品数量
        goods.put("quantity", 1);
        // 商品价格
        goods.put("price", goldCoinGoods.getPrice());
        goodsDetail.add(goods);
        bizContent.put("goods_detail", goodsDetail);

        // 2.执行下单请求
        AlipayTradePrecreateRequest alipayTradePrecreateRequest = new AlipayTradePrecreateRequest();
        alipayTradePrecreateRequest.setReturnUrl(returnUrl);
        alipayTradePrecreateRequest.setNotifyUrl(notifyUrl);
        alipayTradePrecreateRequest.setBizContent(bizContent.toString());
        log.info("封装请求支付宝付款参数为:{}", JSON.toJSONString(alipayTradePrecreateRequest));
        AlipayTradePrecreateResponse response = alipayUtils.execute(alipayTradePrecreateRequest);

        // 3.下单记录保存入库
        threadPool.execute(() -> {
            RLock lock = redissonClient.getLock("alipay:gold_coin_goods_order:" + loginUser.getId());
            if (lock.tryLock()){
                try {
                    // 获得锁成功，执行业务
                    // 保存订单到数据库
                    // 设置订单信息
                    GoldCoinGoodsOrder goldCoinGoodsOrder = new GoldCoinGoodsOrder();
                    goldCoinGoodsOrder.setUserId(loginUser.getId());
                    goldCoinGoodsOrder.setOutTradeNo(response.getOutTradeNo());
                    goldCoinGoodsOrder.setName(goldCoinGoods.getName());
                    goldCoinGoodsOrder.setDescription(goldCoinGoods.getDescription());
                    goldCoinGoodsOrder.setNumber(goldCoinGoods.getNumber());
                    goldCoinGoodsOrder.setTotalAmount(totalAmount);
                    boolean save = goldCoinGoodsOrderService.save(goldCoinGoodsOrder);
                    if (!save) {
                        // 订单记录保存失败
                        log.info("==============================");
                        log.info("用户ID: {}，订单号: {} 的订单记录保存失败！", loginUser.getId(), outTradeNo);
                        log.info("==============================");
                    }
                }finally {
                    lock.unlock();
                }
            }
        });
        // 4.返回结果，主要是返回 qr_code，前端根据 qr_code 进行重定向或者生成二维码引导用户支付
        JSONObject jsonObject = new JSONObject();
        //支付宝响应的订单号
        jsonObject.put("outTradeNo", response.getOutTradeNo());
        //二维码地址，页面使用二维码工具显示出来就可以了
        jsonObject.put("qrCode", response.getQrCode());
        jsonObject.put("totalAmount", totalAmount);

        // 5.将订单二维码保存到redis中，并设置过期时间为5分钟
        String key = "DuckAPI:Order:outTradeNo:"+loginUser.getId()+":"+outTradeNo;
        redisTemplate.opsForValue().set(key, response.getQrCode(), 5, TimeUnit.MINUTES);

        return ResultUtils.success(jsonObject);
    }

    /**
     * 删除订单
     *
     * @param deleteRequest 订单ID
     * @return 删除是否成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteGoldCoinGoodsOrder(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 确保是用户删除的是自己的订单
        QueryWrapper<GoldCoinGoodsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", deleteRequest.getId());
        queryWrapper.eq("user_id", loginUser.getId());
        boolean success = goldCoinGoodsOrderService.remove(queryWrapper);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "删除订单失败");
        return ResultUtils.success(true);
    }

    /**
     * 批量删除
     *
     * @param ids id数组
     * @return 删除是否成功
     */
    @PostMapping("/batch/delete")
    public BaseResponse<Boolean> deleteGoldCoinGoodsOrderByIds(@RequestBody List<Long> ids, HttpServletRequest request) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        for (Long id : ids) {
            ThrowUtils.throwIf(id == null, ErrorCode.NOT_FOUND_ERROR);
            // 判断是否存在
            GoldCoinGoodsOrder goldCoinGoodsOrder = goldCoinGoodsOrderService.getById(id);
            ThrowUtils.throwIf(goldCoinGoodsOrder == null, ErrorCode.NOT_FOUND_ERROR);
            ThrowUtils.throwIf(!goldCoinGoodsOrder.getUserId().equals(loginUser.getId()), ErrorCode.PARAMS_ERROR);
        }
        boolean b = goldCoinGoodsOrderService.removeBatchByIds(ids);
        return ResultUtils.success(b);
    }

    /**
     * 根据 id 获取订单
     *
     * @param id 订单ID
     * @return 订单信息
     */
    @GetMapping("/get")
    public BaseResponse<GoldCoinGoodsOrder> getGoldCoinGoodsOrderById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<GoldCoinGoodsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("user_id", loginUser.getId());
        GoldCoinGoodsOrder goldCoinGoodsOrder = goldCoinGoodsOrderService.getOne(queryWrapper);
        ThrowUtils.throwIf(goldCoinGoodsOrder == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(goldCoinGoodsOrder);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id 订单ID
     * @return 订单信息
     */
    @GetMapping("/get/vo")
    public BaseResponse<GoldCoinGoodsOrderVO> getGoldCoinGoodsOrderVoById(long id, HttpServletRequest request) {
        BaseResponse<GoldCoinGoodsOrder> response = getGoldCoinGoodsOrderById(id, request);
        GoldCoinGoodsOrder goldCoinGoodsOrder = response.getData();
        return ResultUtils.success(goldCoinGoodsOrderService.getGoldCoinGoodsOrderVO(goldCoinGoodsOrder));
    }

    /**
     * 分页获取订单列表
     *
     * @param goldCoinGoodsOrderQueryRequest 订单查询条件
     * @return 订单列表
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<GoldCoinGoodsOrder>> listGoldCoinGoodsOrderByPage(@RequestBody GoldCoinGoodsOrderQueryRequest goldCoinGoodsOrderQueryRequest, HttpServletRequest request) {
        long current = goldCoinGoodsOrderQueryRequest.getCurrent();
        long size = goldCoinGoodsOrderQueryRequest.getPageSize();
        User loginUser = userService.getLoginUser(request);
        goldCoinGoodsOrderQueryRequest.setUserId(loginUser.getId());
        Page<GoldCoinGoodsOrder> goldCoinGoodsOrderPage = goldCoinGoodsOrderService.page(new Page<>(current, size),
                goldCoinGoodsOrderService.getQueryWrapper(goldCoinGoodsOrderQueryRequest));
        return ResultUtils.success(goldCoinGoodsOrderPage);
    }

    /**
     * 分页获取订单封装列表
     *
     * @param goldCoinGoodsOrderQueryRequest 订单查询条件
     * @return 订单列表
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<GoldCoinGoodsOrderVO>> listGoldCoinGoodsOrderVoByPage(@RequestBody GoldCoinGoodsOrderQueryRequest goldCoinGoodsOrderQueryRequest, HttpServletRequest request) {
        if (goldCoinGoodsOrderQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = goldCoinGoodsOrderQueryRequest.getCurrent();
        long size = goldCoinGoodsOrderQueryRequest.getPageSize();
        User loginUser = userService.getLoginUser(request);
        goldCoinGoodsOrderQueryRequest.setUserId(loginUser.getId());
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<GoldCoinGoodsOrder> goldCoinGoodsOrderPage = goldCoinGoodsOrderService.page(new Page<>(current, size),
                goldCoinGoodsOrderService.getQueryWrapper(goldCoinGoodsOrderQueryRequest));
        Page<GoldCoinGoodsOrderVO> goldCoinGoodsOrderVoPage = new Page<>(current, size, goldCoinGoodsOrderPage.getTotal());
        List<GoldCoinGoodsOrderVO> goldCoinGoodsOrderVO = goldCoinGoodsOrderService.getGoldCoinGoodsOrderVO(goldCoinGoodsOrderPage.getRecords());
        goldCoinGoodsOrderVoPage.setRecords(goldCoinGoodsOrderVO);
        return ResultUtils.success(goldCoinGoodsOrderVoPage);
    }

    /**
     * 取消订单
     *
     * @param id 订单的ID
     * @param request request请求
     * @return 取消订单是否成功
     */
    @Transactional
    @PostMapping("/goldCoinGoodsOrder/cancel")
    public BaseResponse<Boolean> cancelGoldCoinGoodsOrder(Long id, HttpServletRequest request){
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 更新数据库中订单状态
        UpdateWrapper<GoldCoinGoodsOrder> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.eq("user_id", loginUser.getId());
        updateWrapper.eq("status", 0);
        updateWrapper.set("status", 2);
        boolean success = goldCoinGoodsOrderService.update(updateWrapper);
        ThrowUtils.throwIf(!success, ErrorCode.OPERATION_ERROR, "订单不存在或订单已取消");

        // 查询订单
        QueryWrapper<GoldCoinGoodsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("user_id", loginUser.getId());
        GoldCoinGoodsOrder goldCoinGoodsOrder = goldCoinGoodsOrderService.getOne(queryWrapper);
        ThrowUtils.throwIf(goldCoinGoodsOrder == null, ErrorCode.NOT_FOUND_ERROR, "订单不存在或已经删除");

        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", goldCoinGoodsOrder.getOutTradeNo());
        AlipayTradeCancelRequest alipayTradeCancelRequest = new AlipayTradeCancelRequest();
        alipayTradeCancelRequest.setBizContent(bizContent.toString());

        // 执行取消订单请求
        alipayUtils.execute(alipayTradeCancelRequest);

        // 删除redis中存储的支付二维码
        String key = "DuckAPI:Order:outTradeNo:"+goldCoinGoodsOrder.getUserId()+":"+goldCoinGoodsOrder.getOutTradeNo();
        Boolean delete = redisTemplate.delete(key);
        if (delete == null || !delete){
            log.info("==============================");
            log.info("删除redis中的支付二维码失败！用户ID：{}，订单号：{}", goldCoinGoodsOrder.getUserId(), goldCoinGoodsOrder.getOutTradeNo());
            log.info("==============================");
        }

        return ResultUtils.success(true);
    }

    /**
     * 获取订单二维码
     *
     * @param id 订单ID
     * @param request request请求
     * @return 订单支付二维码
     */
    @Transactional
    @PostMapping("/goldCoinGoodsOrder/qrCode")
    public BaseResponse<String> getGoldCoinGoodsOrderQrCode(Long id, HttpServletRequest request){
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        //获取订单信息
        BaseResponse<GoldCoinGoodsOrder> response = getGoldCoinGoodsOrderById(id, request);
        GoldCoinGoodsOrder goldCoinGoodsOrder = response.getData();

        //从redis中获取订单二维码链接
        String key = "DuckAPI:Order:outTradeNo:"+loginUser.getId()+":"+goldCoinGoodsOrder.getOutTradeNo();
        String qrCode = redisTemplate.opsForValue().get(key);
        ThrowUtils.throwIf(StrUtil.isBlank(qrCode), ErrorCode.PARAMS_ERROR, "订单已支付或删除");
        return ResultUtils.success(qrCode);
    }

    // endregion

    /**
     * 订单支付成功回调
     * @param request 请求
     */
    @Transactional
    @PostMapping("/success")
    public void success(HttpServletRequest request) {
        // 1.参数校验
        Map<String, String[]> map = request.getParameterMap();
        if (map == null || map.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String[] outTradeNos = map.get("out_trade_no");
        String[] tradeStatuses = map.get("trade_status");
        String[] receiptAmounts = map.get("receipt_amount");
        if (outTradeNos.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (tradeStatuses.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (receiptAmounts.length == 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String outTradeNo = outTradeNos[0];
        String tradeStatus = tradeStatuses[0];
        String receiptAmount = receiptAmounts[0];
        Map<String, Object> message = new HashMap<>();
        message.put("status", false);
        message.put("outTradeNo", outTradeNo);
        // 2.验证签名
        boolean signVerified = alipayUtils.verifySign(map);
        if (!signVerified){
            // 验签失败
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 验签成功，判断订单支付状态
        if ("TRADE_SUCCESS".equals(tradeStatus)) {
            // 2.数据库查询订单
            QueryWrapper<GoldCoinGoodsOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("out_trade_no", outTradeNo);
            GoldCoinGoodsOrder goldCoinGoodsOrder = goldCoinGoodsOrderService.getOne(queryWrapper);
            if (goldCoinGoodsOrder == null) {
                // 订单为空，交易失败
                rabbitProduct.sendMessage(message);
                return;
            }
            Integer status = goldCoinGoodsOrder.getStatus();
            if (status == 1) {
                // 订单已经支付
                message.put("status", true);
                rabbitProduct.sendMessage(message);
                return;
            }
            RLock lock = redissonClient.getLock("alipay:pay_response:" + outTradeNo);
            if (lock.tryLock()){
                try {
                    // 获得锁成功，执行业务
                    // 3.更新数据库订单状态
                    Callable<Boolean> updateGoldCoinGoodsOrderStatus = () -> {
                        UpdateWrapper<GoldCoinGoodsOrder> goldCoinGoodsOrderUpdateWrapper = new UpdateWrapper<>();
                        goldCoinGoodsOrderUpdateWrapper.eq("out_trade_no", outTradeNo);
                        goldCoinGoodsOrderUpdateWrapper.set("status", 1);
                        goldCoinGoodsOrderUpdateWrapper.set("pay_amount", receiptAmount);
                        boolean success = goldCoinGoodsOrderService.update(goldCoinGoodsOrderUpdateWrapper);
                        if (!success) {
                            // 订单状态更新失败
                            rabbitProduct.sendMessage(message);
                        }
                        return success;
                    };

                    Callable<Boolean> queryUserInterfaceInfo = () -> {
                        // 4.更新用户钱包余额
                        Long userId = goldCoinGoodsOrder.getUserId();
                        Integer number = goldCoinGoodsOrder.getNumber();
                        UpdateWrapper<User> userUpdateWrapper = new UpdateWrapper<>();
                        userUpdateWrapper.eq("id", userId);
                        userUpdateWrapper.setSql("gold_coin_balance = gold_coin_balance+"+number);
                        boolean success = userService.update(userUpdateWrapper);
                        if (!success) {
                            // 用户钱包更新失败
                            rabbitProduct.sendMessage(message);
                        }
                        return success;
                    };

                    List<Callable<Boolean>> tasks = new ArrayList<>();
                    tasks.add(updateGoldCoinGoodsOrderStatus);
                    tasks.add(queryUserInterfaceInfo);

                    ExecutorService executorService = Executors.newFixedThreadPool(2);
                    try {
                        List<Future<Boolean>> futures = executorService.invokeAll(tasks);
                        for (Future<Boolean> future : futures) {
                            Boolean result = future.get();
                            if (!result){
                                log.info("==============================");
                                log.info("用户ID: {}", goldCoinGoodsOrder.getUserId());
                                log.info("订单号: {}", goldCoinGoodsOrder.getOutTradeNo());
                                log.info("购买金币数量: {}", goldCoinGoodsOrder.getNumber());
                                log.info("用户订单状态或剩余接口调用次数更新失败！");
                                log.info("==============================");
                            }
                        }
                    } catch (InterruptedException | ExecutionException exception) {
                        Throwable e = exception.getCause();
                        if (e == null){
                            e = exception;
                        }
                        log.info("==============================");
                        log.info("中断异常，异常信息: {}", e.getMessage());
                        log.info("==============================");
                    }
                }finally {
                    lock.unlock();
                }
            }
            // 支付成功，删除redis中的支付二维码
            String key = "DuckAPI:Order:outTradeNo:"+goldCoinGoodsOrder.getUserId()+":"+outTradeNo;
            Boolean success = redisTemplate.delete(key);
            if (success == null || !success){
                log.info("==============================");
                log.info("删除redis中的支付二维码失败！用户ID：{}，订单号：{}", goldCoinGoodsOrder.getUserId(), outTradeNo);
                log.info("==============================");
            }
            message.put("status", true);
        }
        rabbitProduct.sendMessage(message);
    }
}
