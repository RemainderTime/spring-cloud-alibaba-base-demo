package com.xf.clouduser.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @Description: 网关拦截器处理类
 * @ClassName: InternalAuthInterceptor
 * @Author: xiongfeng
 * @Date: 2025/9/1 22:57
 * @Version: 1.0
 */
@Component
public class InternalAuthInterceptor implements HandlerInterceptor {

	private static final String INTERNAL_SECRET = "expected-secret";

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		String header = request.getHeader("X-Internal-Auth");
		if (!INTERNAL_SECRET.equals(header)) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.getWriter().write("Forbidden: Invalid Gateway Access");
			return false;
		}
		return true;
	}
}
