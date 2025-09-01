package com.xf.clouduser.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xf.clouduser.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Description:
 * @ClassName: UserMapper
 * @Author: xiongfeng
 * @Date: 2025/9/1 21:54
 * @Version: 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
