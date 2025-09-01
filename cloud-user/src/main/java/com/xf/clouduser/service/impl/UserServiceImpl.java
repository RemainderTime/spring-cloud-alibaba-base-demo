package com.xf.clouduser.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xf.clouduser.mapper.UserMapper;
import com.xf.clouduser.model.RetObj;
import com.xf.clouduser.model.entity.User;
import com.xf.clouduser.model.req.LoginInfoReq;
import com.xf.clouduser.model.vo.LoginUser;
import com.xf.clouduser.service.UserService;
import com.xf.clouduser.utils.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @ClassName: UserServiceImpl
 * @Author: xiongfeng
 * @Date: 2025/9/1 21:53
 * @Version: 1.0
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

	@Autowired
	private RedisTemplate redisTemplate;

	@Override
	public RetObj login(LoginInfoReq req) {
		//校验登录账号密码
		LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper();
		queryWrapper.eq(User::getAccount, req.getAccount());
		queryWrapper.eq(User::getPassword, req.getPassword());
		User user = this.baseMapper.selectOne(queryWrapper);
		if (Objects.isNull(user)) {
			return RetObj.error("账号或密码错误");
		}
		LoginUser loginUser = new LoginUser();
		loginUser.setId(user.getId());
		loginUser.setAccount(user.getAccount());
		loginUser.setName(user.getName());
		loginUser.setPhone(user.getPhone());
		//生成token
		String token = JwtTokenUtils.createToken(user.getId());
		loginUser.setToken(token);
		//缓存token
		redisTemplate.opsForValue().set("alibaba-token:" + token, JSONObject.toJSONString(loginUser), 3600, TimeUnit.SECONDS);
		redisTemplate.opsForValue().set("alibaba_user_login_token:" + user.getId(), token, 3600, TimeUnit.SECONDS);
		return RetObj.success(loginUser);
	}
}
