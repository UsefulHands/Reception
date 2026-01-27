package com.reception.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BaseException extends RuntimeException {
    public BaseException(String message) {
        super(message);
    }
}
