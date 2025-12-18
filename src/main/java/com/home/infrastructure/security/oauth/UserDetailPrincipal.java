package com.home.infrastructure.security.oauth;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.home.domain.user.User;

public class UserDetailPrincipal implements OAuth2User {
	private User user;
	private Collection<? extends GrantedAuthority> authorities;
	private Map<String, Object> attributes;

	public UserDetailPrincipal(User user, Collection<? extends GrantedAuthority> authorities) {
		this.user = user;
		this.authorities = authorities;
	}

	public UserDetailPrincipal(User user, Collection<? extends GrantedAuthority> authorities, Map<String, Object> attributes) {
		this.user = user;
		this.authorities = authorities;
		this.attributes = attributes;
	}

	@Override
	public String getName() {
		return String.valueOf(user.getId());
	}

	public Long getUserId() {
		return user.getId();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	public Map<String, Object> getUserInfo() {
		Map<String, Object> info = new HashMap<>();
		info.put("name", user.getUserName());
		info.put("email", user.getUserEmail());
		info.put("role", user.getRole().toString());
		info.put("userId", user.getId().toString());
		return info;
	}
}
