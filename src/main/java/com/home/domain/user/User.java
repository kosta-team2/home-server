package com.home.domain.user;

import org.hibernate.annotations.Filter;

import com.home.domain.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@Filter(name = "softDeleteFilter", condition = "deleted_at IS NULL")
@SequenceGenerator(
	name = "user_seq",
	sequenceName = "user_id_seq",
	initialValue = 1,
	allocationSize = 1
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
	private Long id;

	@Column(nullable = false)
	private String userName;

	@Column(nullable = false, length = 50)
	private String displayName;

	@Column(nullable = false, unique = false)
	private String userEmail;

	@Column(nullable = false)
	private String profileImage;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private UserRole role;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private SocialType type;

	@Column(name = "provider_id", nullable = false)
	private String providerId;

	@Builder
	public User(Long id, String userName, String displayName, String userEmail, String profileImage, UserRole role,
		SocialType type, String providerId) {
		this.id = id;
		this.userName = userName;
		this.displayName = displayName;
		this.userEmail = userEmail;
		this.profileImage = profileImage;
		this.role = role;
		this.type = type;
		this.providerId = providerId;
	}
}
