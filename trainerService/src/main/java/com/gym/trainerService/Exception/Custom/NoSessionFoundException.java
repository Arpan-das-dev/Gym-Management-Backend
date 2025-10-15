package com.gym.trainerService.Exception.Custom;

public class NoSessionFoundException extends RuntimeException {
    public NoSessionFoundException(String message) {
        super(message);
    }
}
