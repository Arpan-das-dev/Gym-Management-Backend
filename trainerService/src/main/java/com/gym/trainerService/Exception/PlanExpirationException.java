package com.gym.trainerService.Exception;

public class PlanExpirationException extends RuntimeException {
    public PlanExpirationException(String message) {
        super(message);
    }
}
