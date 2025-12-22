package com.home.global.exception.external;

import com.home.global.exception.BusinessException;
import com.home.global.exception.ErrorCode;

public class MapApiException extends BusinessException {
	public MapApiException(ErrorCode errorCode) {
		super(errorCode);
	}

	@Override
	public boolean isNecessaryToLog() {
		return true;
	}
}
