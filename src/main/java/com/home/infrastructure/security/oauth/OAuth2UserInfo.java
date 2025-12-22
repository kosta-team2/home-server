package com.home.infrastructure.security.oauth;

public interface OAuth2UserInfo {
	String getProviderId();
	String getName();
	String getEmail();
	String getImage();
}
