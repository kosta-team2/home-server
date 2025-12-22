package com.home.infrastructure.web.user.dto;

import com.home.domain.user.User;

public record MeMiniResponse(
	String displayName,
	String profileImage
) {
	public static MeMiniResponse from(User u) {
		return new MeMiniResponse(
			u.getDisplayName(),
			u.getProfileImage()
		);
	}
}
