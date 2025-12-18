package com.home.infrastructure.security.oauth.info;

import java.util.Map;

import com.home.infrastructure.security.oauth.OAuth2UserInfo;

public class KakaoUserInfo implements OAuth2UserInfo {
	public static String providerId;
	public static Map<String, Object> account;
	public static Map<String, Object> profile;


	public KakaoUserInfo(Map<String, Object> attributes) {
		providerId = String.valueOf(attributes.get("id"));
		account = (Map<String, Object>) attributes.get("kakao_account");
		profile = (Map<String, Object>) account.get("profile");
	}

	public String getProviderId() {
		return providerId;
	}

	public String getName() {
		return String.valueOf(profile.get("nickname"));
	}

	public String getImage() {
		return String.valueOf(profile.get("thumbnail_image_url"));
	}

	public String getEmail() {
		return String.valueOf(account.get("email"));
	}

}
