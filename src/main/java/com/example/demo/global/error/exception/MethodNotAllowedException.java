package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class MethodNotAllowedException extends BusinessException {
	private static final long serialVersionUID = 1L;
	
	public MethodNotAllowedException(String message) {
        super(ErrorCode.METHOD_NOT_ALLOWED, message);
    }
}