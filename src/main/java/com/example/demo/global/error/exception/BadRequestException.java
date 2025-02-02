package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class BadRequestException extends BusinessException {
	private static final long serialVersionUID = 1L;
	
	public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}