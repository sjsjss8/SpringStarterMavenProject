package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class AccessDeniedException extends BusinessException {
	private static final long serialVersionUID = 1L;

	public AccessDeniedException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
}