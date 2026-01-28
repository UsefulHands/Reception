package com.reception.common.exception;

public record ErrorResponse(
        int status,
        String error,
        String message
) {}
