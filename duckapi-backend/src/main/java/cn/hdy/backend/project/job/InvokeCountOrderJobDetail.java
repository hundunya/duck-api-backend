package cn.hdy.backend.project.job;

import cn.hdy.backend.project.model.entity.InvokeCountOrder;
import cn.hdy.backend.project.service.InvokeCountOrderService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
public class InvokeCountOrderJobDetail {

    @Resource
    private InvokeCountOrderService invokeCountOrderService;

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
        QueryWrapper<InvokeCountOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 0);
        List<InvokeCountOrder> invokeCountOrders = invokeCountOrderService.list(queryWrapper);
        List<Long> ids = new ArrayList<>();
        for (InvokeCountOrder invokeCountOrder : invokeCountOrders) {
            Date createTime = invokeCountOrder.getCreateTime();
            // 创建时间，单位：毫秒
            long createTimeMillis = createTime.getTime();
            // 当前时间，单位：毫秒
            long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - createTimeMillis > ORDER_EXPIRED_TIME){
                ids.add(invokeCountOrder.getId());
            }
        }
        if (!ids.isEmpty()){
            // 批量删除过期数据
            boolean result = invokeCountOrderService.removeBatchByIds(ids);
            if (!result){
                log.info("批量删除过期订单失败！时间: {}", dateFormat.format(new Date()));
            }
        }
    }
}
