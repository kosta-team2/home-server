package com.home.infrastructure.security.oauth.info;

import java.util.Map;

import com.home.infrastructure.security.oauth.OAuth2UserInfo;

public class NaverUserInfo implements OAuth2UserInfo {
	public static String providerId;
	public static Map<String, Object> responseMap;

	public NaverUserInfo(Map<String, Object> attributes) {
		responseMap = (Map<String, Object>)attributes.get("response");
	}

	public String getProviderId() {
		return String.valueOf(responseMap.get("id"));
	}

	public String getEmail() {
		return String.valueOf(responseMap.get("email"));
	}

	public String getName() {
		return String.valueOf(responseMap.get("name"));
	}

	public String getImage() {
		return String.valueOf(responseMap.get("profile_image"));
	}
}


