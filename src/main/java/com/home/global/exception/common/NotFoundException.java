package com.home.global.exception.common;

import com.home.global.exception.BusinessException;
import com.home.global.exception.ErrorCode;

public class NotFoundException extends BusinessException {
	protected NotFoundException(ErrorCode errorCode) {
		super(errorCode);
	}

	protected NotFoundException(ErrorCode errorCode, String message) {
		super(errorCode, message);
	}

	@Override
	public boolean isNecessaryToLog() {
		return true;
	}
}
