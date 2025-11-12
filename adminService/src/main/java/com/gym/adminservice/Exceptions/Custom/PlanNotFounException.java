package com.gym.adminservice.Exceptions.Custom;

public class PlanNotFounException extends RuntimeException {
    public PlanNotFounException(String message) {
        super(message);
    }
}
