package com.home.global.exception.common;

import com.home.global.exception.BusinessException;
import com.home.global.exception.ErrorCode;

public class AccessDeniedException extends BusinessException {
	protected AccessDeniedException(ErrorCode errorCode) {
		super(errorCode);
	}

	protected AccessDeniedException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	@Override
	public boolean isNecessaryToLog() {
		return false;
	}
}
