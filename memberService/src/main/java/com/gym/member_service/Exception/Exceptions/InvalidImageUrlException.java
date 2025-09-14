package com.gym.member_service.Exception.Exceptions;

public class InvalidImageUrlException extends RuntimeException {
    public InvalidImageUrlException(String message) {
        super(message);
    }
}
