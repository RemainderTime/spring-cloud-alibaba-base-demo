package com.xf.gateway.filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * @Description:
 * @ClassName: SecurityConfig
 * @Author: xiongfeng
 * @Date: 2025/9/4 22:41
 * @Version: 1.0
 */
@Configuration
public class SecurityConfig {
	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
				.csrf(csrf -> csrf.disable())  // 禁用 CSRF
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers("/user/user/login", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.anyExchange().authenticated()
				);
		return http.build();
	}
}
