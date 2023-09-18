package cn.hdy.backend.project.controller;

import cn.hdy.backend.project.common.BaseResponse;
import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.common.ResultUtils;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.exception.ThrowUtils;
import cn.hdy.backend.project.job.RabbitProduct;
import cn.hdy.backend.project.model.dto.alipay.AlipayAddRequest;
import cn.hdy.backend.project.model.dto.alipay.AlipayDeleteRequest;
import cn.hdy.backend.project.model.entity.InvokeCountOrder;
import cn.hdy.backend.project.service.InterfaceInfoService;
import cn.hdy.backend.project.service.InvokeCountOrderService;
import cn.hdy.backend.project.service.UserInterfaceService;
import cn.hdy.backend.project.service.UserService;
import cn.hdy.backend.project.utils.AlipayUtils;
import cn.hdy.backend.project.utils.OutTradeNoUtils;
import cn.hdy.common.project.model.entity.InterfaceInfo;
import cn.hdy.common.project.model.entity.User;
import cn.hdy.common.project.model.entity.UserInterface;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.hdy.backend.project.constant.UserConstant.LEFT_NUM;

/**
 * @author 滴滴鸭
 */
@Slf4j
@RestController
@RequestMapping("/alipay")
public class AlipayController {

    //服务器异步通知页面路径
    @Value("${alipay.notifyUrl}")
    private String notifyUrl;

    //页面跳转同步通知页面路径
    @Value("${alipay.returnUrl}")
    private String returnUrl;

    @Resource
    private RabbitProduct rabbitProduct;

    @Resource
    private InvokeCountOrderService invokeCountOrderService;

    @Resource
    private UserService userService;

    @Resource
    private UserInterfaceService userInterfaceService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private CuratorFramework zkClient;

    @Resource
    private AlipayUtils alipayUtils;

    /**
     * 手机扫码支付
     *
     * @param alipayAddRequest 阿里支付实体
     * @param request
     * @return
     */
    @Transactional
    @PostMapping("/order/pay")
    public BaseResponse<JSONObject> payOrder(AlipayAddRequest alipayAddRequest, HttpServletRequest request) throws Exception {
        if (alipayAddRequest == null || alipayAddRequest.getInvokeCount() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (alipayAddRequest.getInterfaceId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口不存在");
        }
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        JSONObject bizContent = new JSONObject();
        // 1.设置商品信息
        // 订单号生成
        String outTradeNo = OutTradeNoUtils.getOutTradeNo();
        bizContent.put("out_trade_no", outTradeNo);
        // 数据库查询接口
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(alipayAddRequest.getInterfaceId());
        Double price = interfaceInfo.getPrice();
        Double totalAmount = alipayAddRequest.getInvokeCount() * price;
        bizContent.put("total_amount", totalAmount);
        bizContent.put("subject", "接口次数购买");

        // 2.执行下单请求
        //扫码支付使用AlipayTradePrecreateRequest传参，下面调用的是execute方法
        AlipayTradePrecreateRequest alipayTradePrecreateRequest = new AlipayTradePrecreateRequest();
        alipayTradePrecreateRequest.setReturnUrl(returnUrl);
        alipayTradePrecreateRequest.setNotifyUrl(notifyUrl);
        alipayTradePrecreateRequest.setBizContent(bizContent.toString());
        log.info("封装请求支付宝付款参数为:{}", JSON.toJSONString(alipayTradePrecreateRequest));
        AlipayTradePrecreateResponse response = alipayUtils.execute(alipayTradePrecreateRequest);

        // 3.下单记录保存入库
        InterProcessMutex zkMutex = new InterProcessMutex(zkClient, "/duckapi/mutex" + loginUser.getId() + "-lock");
        try {
            if (zkMutex.acquire(5L, TimeUnit.SECONDS)) {
                // 获得锁成功，执行业务
                InvokeCountOrder invokeCountOrder = new InvokeCountOrder();
                invokeCountOrder.setOutTradeNo(outTradeNo);
                invokeCountOrder.setTotalAmount(totalAmount);
                invokeCountOrder.setPayAmount(0.0);
                invokeCountOrder.setInterfaceId(alipayAddRequest.getInterfaceId());
                invokeCountOrder.setUserId(loginUser.getId());
                invokeCountOrder.setInvokeCount(alipayAddRequest.getInvokeCount());
                boolean save = invokeCountOrderService.save(invokeCountOrder);
                if (!save) {
                    // 订单记录保存失败
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            } else {
                // 获得锁失败
                throw new RuntimeException();
            }
        } catch (Exception e) {
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } finally {
            zkMutex.release();
        }
        // 4.返回结果，主要是返回 qr_code，前端根据 qr_code 进行重定向或者生成二维码引导用户支付
        JSONObject jsonObject = new JSONObject();
        //支付宝响应的订单号
        outTradeNo = response.getOutTradeNo();
        jsonObject.put("outTradeNo", outTradeNo);
        //二维码地址，页面使用二维码工具显示出来就可以了
        jsonObject.put("qrCode", response.getQrCode());
        jsonObject.put("totalAmount", totalAmount);
        return ResultUtils.success(jsonObject);
    }

    /**
     * 取消订单
     *
     * @param alipayDeleteRequest 取消订单实体
     * @param request
     * @return
     */
    @Transactional
    @PostMapping("/order/cancel")
    public BaseResponse<Boolean> cancelOrder(AlipayDeleteRequest alipayDeleteRequest, HttpServletRequest request){
        if (alipayDeleteRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        QueryWrapper<InvokeCountOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no", alipayDeleteRequest.getOutTradeNo());
        queryWrapper.eq("user_id", loginUser.getId());
        boolean remove = invokeCountOrderService.remove(queryWrapper);
        ThrowUtils.throwIf(!remove, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(true);
    }

    @Transactional
    @PostMapping("/success")
    public void success(HttpServletRequest request) throws Exception {
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
            QueryWrapper<InvokeCountOrder> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("out_trade_no", outTradeNo);
            InvokeCountOrder invokeCountOrder = invokeCountOrderService.getOne(queryWrapper);
            if (invokeCountOrder == null) {
                // 订单为空，交易失败
                rabbitProduct.sendMessage(message);
                return;
            }
            Integer status = invokeCountOrder.getStatus();
            if (status == 1) {
                // 订单已经支付
                message.put("status", true);
                rabbitProduct.sendMessage(message);
                return;
            }
            InterProcessMutex zkMutex = new InterProcessMutex(zkClient, "/duckapi/mutex" + invokeCountOrder.getOutTradeNo() + "-lock");
            try {
                if (zkMutex.acquire(5L, TimeUnit.SECONDS)) {
                    // 获得锁成功，执行业务
                    // 3.更新数据库订单状态
                    UpdateWrapper<InvokeCountOrder> invokeCountOrderUpdateWrapper = new UpdateWrapper<>();
                    invokeCountOrderUpdateWrapper.eq("out_trade_no", outTradeNo);
                    invokeCountOrderUpdateWrapper.set("status", 1);
                    invokeCountOrderUpdateWrapper.set("pay_amount", receiptAmount);
                    boolean success = invokeCountOrderService.update(invokeCountOrderUpdateWrapper);
                    if (!success) {
                        // 订单状态更新失败
                        rabbitProduct.sendMessage(message);
                        return;
                    }
                    Long userId = invokeCountOrder.getUserId();
                    Long interfaceId = invokeCountOrder.getInterfaceId();

                    // 4.查询用户接口信息】
                    QueryWrapper<UserInterface> userInterfaceQueryWrapper = new QueryWrapper<>();
                    userInterfaceQueryWrapper.eq("user_id", userId);
                    userInterfaceQueryWrapper.eq("interface_id", interfaceId);
                    UserInterface userInterface = userInterfaceService.getOne(userInterfaceQueryWrapper);
                    // 5.更新用户接口剩余调用次数
                    if (userInterface == null){
                        // 用户未调用过该接口
                        // 添加信息
                        userInterface = new UserInterface();
                        userInterface.setInterfaceId(interfaceId);
                        userInterface.setUserId(userId);
                        userInterface.setLeftNum(LEFT_NUM+invokeCountOrder.getInvokeCount());
                        userInterface.setTotalNum(0);
                        success = userInterfaceService.save(userInterface);
                    }else {
                        // 用户调用过该接口
                        UpdateWrapper<UserInterface> userInterfaceUpdateWrapper = new UpdateWrapper<>();
                        userInterfaceUpdateWrapper.eq("interface_id", interfaceId);
                        userInterfaceUpdateWrapper.eq("user_id", userId);
                        userInterfaceUpdateWrapper.setSql("left_num = left_num + " + invokeCountOrder.getInvokeCount());
                        success = userInterfaceService.update(userInterfaceUpdateWrapper);
                    }
                    if (!success) {
                        // 用户次数更新失败
                        rabbitProduct.sendMessage(message);
                        return;
                    }
                } else {
                    // 获得锁失败
                    throw new RuntimeException();
                }
            } catch (Exception e) {
                if (e instanceof BusinessException) {
                    throw e;
                } else {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            } finally {
                zkMutex.release();
            }
            message.put("status", true);
        }
        rabbitProduct.sendMessage(message);
    }

}
