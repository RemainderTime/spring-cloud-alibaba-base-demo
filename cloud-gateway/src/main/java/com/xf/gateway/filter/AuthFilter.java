package com.xf.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.xf.gateway.model.LoginUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.security.auth.login.LoginException;

/**
 * @Description:
 * @ClassName: AuthFilter
 * @Author: xiongfeng
 * @Date: 2025/9/1 22:47
 * @Version: 1.0
 */
@Component
public class AuthFilter implements GlobalFilter, Ordered {

	@Autowired
	private RedisTemplate redisTemplate;


	private static final String SECRET_KEY = "expected-secret";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		// 获取 Token
		String token = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
		if (token == null || !token.startsWith("Bearer ")) {
			exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
			return exchange.getResponse().setComplete();
		}
		// 校验 Token（假设用 JWT）
		String jwt = token.substring(7);
		String value = (String) redisTemplate.opsForValue().get("alibaba-token:" + token);
		if (StringUtils.isEmpty(value)) {
			throw new RuntimeException("请先登录");
		}
		JSONObject jsonObject = JSONObject.parseObject(value);
		//JSON对象转换成Java对象
		LoginUser loginUserInfo = JSONObject.toJavaObject(jsonObject, LoginUser.class);
		if (loginUserInfo == null || loginUserInfo.getId() <= 0) {
			throw new RuntimeException("用户登录异常");
		}
		// 修改请求，添加内部 Header（用户信息 + 内部校验）
		ServerHttpRequest mutatedRequest = request.mutate()
				.header("X-User-Id", String.valueOf(loginUserInfo.getId()))
				.header("X-Username", loginUserInfo.getName())
				.header("X-Internal-Auth", SECRET_KEY) // 内部服务防护
				.build();

		return chain.filter(exchange.mutate().request(mutatedRequest).build());
	}

	@Override
	public int getOrder() {
		return -100; // 保证在最前面执行
	}

}
