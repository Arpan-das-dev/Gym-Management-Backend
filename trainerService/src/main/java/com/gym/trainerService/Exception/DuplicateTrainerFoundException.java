package com.gym.trainerService.Exception;

public class DuplicateTrainerFoundException extends RuntimeException {
    public DuplicateTrainerFoundException(String message) {
        super(message);
    }
}
