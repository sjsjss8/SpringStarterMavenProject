package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class InternalServerException extends BusinessException {
	private static final long serialVersionUID = 1L;
	
	public InternalServerException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}