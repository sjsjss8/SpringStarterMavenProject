package com.example.demo.global.error.exception;

import com.example.demo.global.error.ErrorCode;

public class EntityNotFoundException extends BusinessException {
	private static final long serialVersionUID = 1L;
	
	public EntityNotFoundException(String message) {
        super(ErrorCode.ENTITY_NOT_FOUND, message);
    }
}
