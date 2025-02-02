package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class DatabaseException extends BusinessException {
	private static final long serialVersionUID = 1L;
	
	public DatabaseException(String message) {
        super(ErrorCode.DATABASE_ERROR, message);
    }
}