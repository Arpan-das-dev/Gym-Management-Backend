package com.gym.planService.Exception.Custom;

public class DuplicatePlanFoundException extends RuntimeException {
    public DuplicatePlanFoundException(String message) {
        super(message);
    }
}
