package com.gym.planService.Exception.Custom;

public class RefundFailedException extends RuntimeException {
    public RefundFailedException(String message) {
        super(message);
    }
}
