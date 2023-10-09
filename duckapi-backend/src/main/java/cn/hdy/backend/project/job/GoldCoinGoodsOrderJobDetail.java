package cn.hdy.backend.project.job;

import cn.hdy.backend.project.model.entity.GoldCoinGoodsOrder;
import cn.hdy.backend.project.service.GoldCoinGoodsOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 滴滴鸭
 */
@Data
@Slf4j
@Component
public class GoldCoinGoodsOrderJobDetail {

    @Resource
    private GoldCoinGoodsOrderService goldCoinGoodsOrderService;

    /**
     * 订单5分钟过期
     */
    private final long ORDER_EXPIRED_TIME = 5 * 60 * 1000;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 执行任务
     */
    public void doJob(){
        clearExpiredOrders();
    }

    /**
     * 清理过期订单数据
     */
    private void clearExpiredOrders(){
        // 1.查询所有未支付的订单
        QueryWrapper<GoldCoinGoodsOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0);
        List<GoldCoinGoodsOrder> goldCoinGoodsOrders = goldCoinGoodsOrderService.list(queryWrapper);
        List<Long> ids = new ArrayList<>();
        for (GoldCoinGoodsOrder goldCoinGoodsOrder : goldCoinGoodsOrders) {
            Date createTime = goldCoinGoodsOrder.getCreateTime();
            // 创建时间，单位：毫秒
            long createTimeMillis = createTime.getTime();
            // 当前时间，单位：毫秒
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - createTimeMillis > ORDER_EXPIRED_TIME){
                ids.add(goldCoinGoodsOrder.getId());
            }
        }
        if (!ids.isEmpty()){
            // 批量取消过期数据
            UpdateWrapper<GoldCoinGoodsOrder> updateWrapper = new UpdateWrapper<>();
            updateWrapper.set("status", 2);
            updateWrapper.in("id", ids);
            boolean result = goldCoinGoodsOrderService.update(updateWrapper);
            if (!result){
                log.info("批量删除过期订单失败！时间: {}", dateFormat.format(new Date()));
            }
        }
    }
}
