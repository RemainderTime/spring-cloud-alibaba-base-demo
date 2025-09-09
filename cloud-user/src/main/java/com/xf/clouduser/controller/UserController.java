package com.xf.clouduser.controller;

import com.xf.cloudcommon.utils.UserContextHolder;
import com.xf.clouduser.model.RetObj;
import com.xf.clouduser.model.req.LoginInfoReq;
import com.xf.clouduser.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description: 用户控制器
 * @ClassName: UserController
 * @Author: xiongfeng
 * @Date: 2025/9/1 21:51
 * @Version: 1.0
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserService userService;
    /**
     * 登录
     * @param req
     * @return
     */
	@PostMapping("/login")
	public RetObj login(@RequestBody LoginInfoReq req){
		return userService.login(req);
	}

    /**
     * 获取登录用户名称
     * @return
     */
    @GetMapping("/getUserName")
    public RetObj getUserName(){
        return RetObj.success(UserContextHolder.getName());
    }
}
