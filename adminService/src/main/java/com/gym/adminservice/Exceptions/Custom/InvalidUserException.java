package com.gym.adminservice.Exceptions.Custom;

public class InvalidUserException extends RuntimeException {
    public InvalidUserException(String message) {
        super(message);
    }
}
