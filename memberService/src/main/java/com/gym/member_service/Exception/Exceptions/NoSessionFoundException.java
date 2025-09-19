package com.gym.member_service.Exception.Exceptions;

public class NoSessionFoundException extends RuntimeException {
    public NoSessionFoundException(String message) {
        super(message);
    }
}
