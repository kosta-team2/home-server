package com.home.infrastructure.security.oauth.info;

import java.util.Map;

import com.home.infrastructure.security.oauth.OAuth2UserInfo;

import lombok.Getter;

@Getter
public class GoogleUserInfo implements OAuth2UserInfo {
	private final String providerId;
	private final String email;
	private final String name;
	private final String image;

	public GoogleUserInfo(Map<String, Object> attributes) {
		this.providerId = String.valueOf(attributes.get("sub"));
		this.email = String.valueOf(attributes.get("email"));
		this.name = String.valueOf(attributes.get("name"));
		this.image = String.valueOf(attributes.get("picture"));
	}
}
