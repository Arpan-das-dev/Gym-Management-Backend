package com.gym.planService.Exception.Custom;

public class DuplicateCuponCodeFoundException extends RuntimeException {
    public DuplicateCuponCodeFoundException(String message) {
        super(message);
    }
}
