package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class InvalidValueException extends BusinessException {
	private static final long serialVersionUID = 1L;
	
	public InvalidValueException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}