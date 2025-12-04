package com.gym.member_service.Exception.Exceptions;

public class UnAuthorizedRequestException extends RuntimeException {
    public UnAuthorizedRequestException(String message) {
        super(message);
    }
}
