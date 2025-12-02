package com.home.global.exception.external;

import com.home.global.exception.BusinessException;
import com.home.global.exception.ErrorCode;

public class ExternalApiException extends BusinessException {

	public ExternalApiException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	@Override
	public boolean isNecessaryToLog() {
		return true;
	}
}
