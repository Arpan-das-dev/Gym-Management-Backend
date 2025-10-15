package com.gym.trainerService.Exception.Custom;

public class DuplicateSpecialtyFoundException extends RuntimeException {
    public DuplicateSpecialtyFoundException(String message) {
        super(message);
    }
}
