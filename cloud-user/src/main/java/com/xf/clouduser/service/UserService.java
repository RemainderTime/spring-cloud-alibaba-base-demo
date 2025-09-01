package com.xf.clouduser.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xf.clouduser.model.RetObj;
import com.xf.clouduser.model.entity.User;
import com.xf.clouduser.model.req.LoginInfoReq;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description:
 * @ClassName: UserSrvice
 * @Author: xiongfeng
 * @Date: 2025/9/1 21:53
 * @Version: 1.0
 */
public interface UserService extends IService<User> {

	RetObj login(@RequestBody LoginInfoReq req);
}
