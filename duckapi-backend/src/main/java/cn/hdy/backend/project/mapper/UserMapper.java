package cn.hdy.backend.project.mapper;

import cn.hdy.common.project.model.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 用户数据库操作
 *
 * @author 滴滴鸭
 */
public interface UserMapper extends BaseMapper<User> {

    String selectAccessKeyById(Long id);

    String selectSecretKeyById(Long id);
}




