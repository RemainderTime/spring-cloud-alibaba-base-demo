package com.xf.gateway.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * @Description: 自定义Spring Security用户对象类
 * @ClassName: CustomUserDetails
 * @Author: xiongfeng
 * @Date: 2025/8/27 20:58
 * @Version: 1.0
 */
@Data
public class CustomUserDetails implements UserDetails {

	/**
	 * 用户ID
	 */
	private Integer userId;

	/**
	 * 手机号
	 */
	private String phone;

	/**
	 * 权限列表
	 */
	private Collection<? extends GrantedAuthority> authorities;

	public CustomUserDetails(Integer userId, String phone, Collection<? extends GrantedAuthority> authorities) {
		this.userId = userId;
		this.phone = phone;
		this.authorities = authorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return null;
	}
}
