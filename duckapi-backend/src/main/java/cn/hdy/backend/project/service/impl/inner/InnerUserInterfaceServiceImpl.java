package cn.hdy.backend.project.service.impl.inner;

import cn.hdy.backend.project.common.ErrorCode;
import cn.hdy.backend.project.exception.BusinessException;
import cn.hdy.backend.project.mapper.UserInterfaceMapper;
import cn.hdy.common.project.model.entity.UserInterface;
import cn.hdy.common.project.service.InnerUserInterfaceService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static cn.hdy.backend.project.constant.UserConstant.LEFT_NUM;

/**
 * @author 混沌鸭
 **/
@DubboService
public class InnerUserInterfaceServiceImpl extends ServiceImpl<UserInterfaceMapper, UserInterface>
        implements InnerUserInterfaceService {
    @Resource
    private UserInterfaceMapper userInterfaceMapper;

    @Resource
    private CuratorFramework zkClient;

    @Override
    public void invokeCount(long interfaceId, long userId) throws Exception {
        InterProcessMutex zkMutex = new InterProcessMutex(zkClient, "/duck/api/mutex"+userId+"-lock");
        try {
            if (zkMutex.acquire(5L, TimeUnit.SECONDS)) {
                // 获得锁成功，执行业务
                if (interfaceId <= 0 || userId <= 0){
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
                }
                // 1、查询数据库中是否存在该计数记录
                QueryWrapper<UserInterface> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("interface_id", interfaceId);
                queryWrapper.eq("user_id", userId);
                UserInterface userInterface = userInterfaceMapper.selectOne(queryWrapper);
                // 2、不存在记录则添加记录，并给用户默认分配100次调用次数
                if (userInterface == null){
                    // 添加记录
                    userInterface = new UserInterface();
                    userInterface.setInterfaceId(interfaceId);
                    userInterface.setUserId(userId);
                    userInterface.setLeftNum(LEFT_NUM);
                    userInterface.setTotalNum(0);
                    boolean success = this.save(userInterface);
                    if (!success){
                        throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                    }
                }
                // 3、判断用户是否还有调用次数
                if (userInterface.getLeftNum() <=0){
                    throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "接口调用次数已用完");
                }
                // 4、用户对该接口的总调用次数+1，剩余调用次数-1
                UpdateWrapper<UserInterface> updateWrapper = new UpdateWrapper<>();
                updateWrapper.gt("left_num", 0);
                updateWrapper.set("total_num", userInterface.getTotalNum()+1);
                updateWrapper.set("left_num", userInterface.getLeftNum()-1);
                boolean success = this.update(updateWrapper);
                if (!success){
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
            }else {
                // 获得锁失败
                throw new RuntimeException();
            }
        } catch (Exception e) {
            if (e instanceof BusinessException){
                throw e;
            }else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        } finally {
            zkMutex.release();
        }
    }
}
