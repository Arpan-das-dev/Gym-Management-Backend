package com.gym.trainerService.Exception.Custom;

public class DuplicateTrainerFoundException extends RuntimeException {
    public DuplicateTrainerFoundException(String message) {
        super(message);
    }
}
