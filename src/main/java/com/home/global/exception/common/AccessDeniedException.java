package com.home.global.exception.common;

import com.home.global.exception.BusinessException;
import com.home.global.exception.ErrorCode;

public class AccessDeniedException extends BusinessException {
	public AccessDeniedException(ErrorCode errorCode) {
		super(errorCode);
	}

	public AccessDeniedException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	@Override
	public boolean isNecessaryToLog() {
		return false;
	}
}
