package cn.hdy.backend.project.service.impl.inner;

import cn.hdy.backend.project.mapper.UserMapper;
import cn.hdy.common.project.model.entity.User;
import cn.hdy.common.project.service.InnerUserService;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author 混沌鸭
 **/
@DubboService
public class InnerUserServiceImpl extends ServiceImpl<UserMapper, User>
        implements InnerUserService {
    @Resource
    private UserMapper userMapper;

    @Override
    public User getInvokeUser(String accessKey) {
        if (StrUtil.isBlank(accessKey)){
            return null;
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("access_key", accessKey);
        User user = userMapper.selectOne(queryWrapper);
        user.setUserPassword(null);
        return user;
    }
}
