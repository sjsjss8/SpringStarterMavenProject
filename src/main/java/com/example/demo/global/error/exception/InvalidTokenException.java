package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class InvalidTokenException extends BusinessException {
	private static final long serialVersionUID = 1L;
	
	public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
}