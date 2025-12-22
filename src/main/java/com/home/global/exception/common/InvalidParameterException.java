package com.home.global.exception.common;

import com.home.global.exception.BusinessException;
import com.home.global.exception.ErrorCode;

public class InvalidParameterException extends BusinessException {
	public InvalidParameterException(ErrorCode errorCode) {
		super(errorCode);
	}

	public InvalidParameterException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	@Override
	public boolean isNecessaryToLog() {
		return false;
	}
}
