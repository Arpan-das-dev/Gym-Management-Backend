package com.gym.planService.Exception.Custom;

public class EmailSendFailedException extends RuntimeException {
    public EmailSendFailedException(String message) {
        super(message);
    }
}
