package com.home.infrastructure.security.oauth;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.home.domain.user.SocialType;
import com.home.domain.user.User;
import com.home.domain.user.UserRepository;
import com.home.domain.user.UserRole;
import com.home.infrastructure.security.oauth.info.GoogleUserInfo;
import com.home.infrastructure.security.oauth.info.KakaoUserInfo;
import com.home.infrastructure.security.oauth.info.NaverUserInfo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);
		Map<String, Object> attributes = oAuth2User.getAttributes();
		String registrationId = userRequest.getClientRegistration().getRegistrationId();
		SocialType socialType = SocialType.valueOf(registrationId.toUpperCase());

		OAuth2UserInfo userInfo = switch (registrationId) {
			case "google" -> new GoogleUserInfo(attributes);
			case "naver" -> new NaverUserInfo(attributes);
			default -> new KakaoUserInfo(attributes);
		};

		String providerId = userInfo.getProviderId();
		String name = userInfo.getName();
		String email = userInfo.getEmail();
		String profile = userInfo.getImage();
		String displayName = userInfo.getName();

		Optional<User> byProviderId = userRepository.findByProviderId(providerId);
		User user = byProviderId.orElseGet(
			() -> saveSocialMember(providerId, name, email, profile, displayName, socialType));

		return new UserDetailPrincipal(user,
			Collections.singleton(new SimpleGrantedAuthority(user.getRole().getValue())), attributes);
	}

	public User saveSocialMember(String providerId, String name, String email, String profile, String displayName,
		SocialType type) {
		User newMember = User.builder()
			.providerId(providerId)
			.userName(name)
			.userEmail(email)
			.profileImage(profile)
			.role(UserRole.USER)
			.displayName(displayName)
			.type(type)
			.build();
		return userRepository.save(newMember);
	}
}
