package com.example.elkmonitoring.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus status;

    public BusinessException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public BusinessException(String message, String errorCode) {
        this(message, errorCode, HttpStatus.BAD_REQUEST);
    }
}
