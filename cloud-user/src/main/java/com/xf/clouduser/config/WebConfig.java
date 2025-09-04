package com.xf.clouduser.config;

import com.xf.clouduser.interceptor.InternalAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Description: Spring MVC 配置类
 * @ClassName: WebConfig
 * @Author: xiongfeng
 * @Date: 2025/9/4 23:15
 * @Version: 1.0
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final InternalAuthInterceptor internalAuthInterceptor;

	public WebConfig(InternalAuthInterceptor internalAuthInterceptor) {
		this.internalAuthInterceptor = internalAuthInterceptor;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 只针对敏感接口启用
		registry.addInterceptor(internalAuthInterceptor);
//				.addPathPatterns("/admin/**", "/internal/**"); // 只拦截特定内部接口
	}
}
