package com.gym.trainerService.Exception.Custom;

public class PlanExpirationException extends RuntimeException {
    public PlanExpirationException(String message) {
        super(message);
    }
}
