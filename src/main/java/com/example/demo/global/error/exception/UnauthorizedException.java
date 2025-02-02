package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class UnauthorizedException extends BusinessException {
	private static final long serialVersionUID = 1L;
	
	public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}