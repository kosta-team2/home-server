package com.home.domain.favorite;

import org.hibernate.annotations.Filter;

import com.home.domain.common.BaseEntity;
import com.home.domain.parcel.Parcel;
import com.home.domain.user.User;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
	name = "favorite_parcel",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_favorite_user_parcel", columnNames = {"user_id", "parcel_id"})
	}
)
@Filter(name = "softDeleteFilter", condition = "deleted_at IS NULL")
@SequenceGenerator(
	name = "favorite_parcel_seq",
	sequenceName = "favorite_parcel_id_seq",
	initialValue = 1,
	allocationSize = 50
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteParcel extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "favorite_parcel_seq")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parcel_id", nullable = false)
	private Parcel parcel;

	@Column(name = "complex_name", nullable = false)
	private String complexName;

	@Column(name = "alarm_enabled", nullable = false)
	private boolean alarmEnabled = true;

	private FavoriteParcel(User user, Parcel parcel, String complexName, boolean alarmEnabled) {
		this.user = user;
		this.parcel = parcel;
		this.complexName = complexName;
		this.alarmEnabled = alarmEnabled;
	}

	public static FavoriteParcel create(User user, Parcel parcel, String complexName) {
		return new FavoriteParcel(user, parcel, complexName, true);
	}

	public void setAlarmEnabled(boolean enabled) {
		this.alarmEnabled = enabled;
	}
}
