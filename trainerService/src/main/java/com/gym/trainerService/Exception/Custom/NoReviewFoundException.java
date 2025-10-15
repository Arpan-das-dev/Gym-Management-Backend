package com.gym.trainerService.Exception.Custom;

public class NoReviewFoundException extends RuntimeException {
    public NoReviewFoundException(String message) {
        super(message);
    }
}
