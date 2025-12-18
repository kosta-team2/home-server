package com.home.global.exception.common;

import com.home.global.exception.BusinessException;
import com.home.global.exception.ErrorCode;

public class UnauthorizedException extends BusinessException {
	public UnauthorizedException() {
		super(ErrorCode.AUTH_ERROR);
	}

	public UnauthorizedException(String message) {
		super(ErrorCode.AUTH_ERROR, message);
	}
	@Override
	public boolean isNecessaryToLog() {
		return true;
	}
}
