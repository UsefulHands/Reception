package com.github.UsefulHands.reception.common.exception;

public record ErrorResponse(
        int status,
        String error,
        String message
) {}
