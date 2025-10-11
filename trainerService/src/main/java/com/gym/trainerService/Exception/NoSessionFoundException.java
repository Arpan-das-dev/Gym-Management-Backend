package com.gym.trainerService.Exception;

public class NoSessionFoundException extends RuntimeException {
    public NoSessionFoundException(String message) {
        super(message);
    }
}
