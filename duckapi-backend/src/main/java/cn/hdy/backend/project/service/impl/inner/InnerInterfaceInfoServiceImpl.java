package cn.hdy.backend.project.service.impl.inner;

import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.mapper.InterfaceInfoMapper;
import cn.hdy.common.project.model.entity.InterfaceInfo;
import cn.hdy.common.project.service.InnerInterfaceInfoService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author 混沌鸭
 **/
@Slf4j
@DubboService
public class InnerInterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InnerInterfaceInfoService {
    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(10);

    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        if (StrUtil.isBlank(path) || StrUtil.isBlank(method)){
            return null;
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", path);
        queryWrapper.eq("method", method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public void invokeCount(long interfaceId) {
        if (interfaceId <= 0){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 直接从redis中查询数据
        String key = "InterfaceInfo:TotalNum:"+interfaceId;
        String totalNum = stringRedisTemplate.opsForValue().get(key);
        if (totalNum == null){
            // redis中未查询到数据
            // 1、从数据库中查询该接口信息
            InterfaceInfo interfaceInfo = interfaceInfoMapper.selectById(interfaceId);
            // 2、接口不存在
            if (interfaceInfo == null){
                log.info("==============================");
                log.info("接口计数失败，接口不存在！");
                log.info("==============================");
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
            }
            totalNum = String.valueOf(interfaceInfo.getTotalNum());
            stringRedisTemplate.opsForValue().set(key, totalNum);
        }
        // 将接口信息存入redis
        stringRedisTemplate.opsForValue().increment(key);
        // 异步执行接口调用次数+1操作到数据库中
        threadPool.execute(() -> {
            // 接口调用次数+1
            UpdateWrapper<InterfaceInfo> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", interfaceId);
            updateWrapper.setSql("total_num = total_num + 1");
            boolean success = this.update(updateWrapper);
            if (!success){
                log.info("==============================");
                log.info("接口调用次数更新失败！");
                log.info("==============================");
            }
        });
    }
}
