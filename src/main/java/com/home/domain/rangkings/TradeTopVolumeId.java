package com.home.domain.rangkings;

import java.io.Serializable;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode
public class TradeTopVolumeId implements Serializable {
	private Long regionId;
	private Integer rank;
}
