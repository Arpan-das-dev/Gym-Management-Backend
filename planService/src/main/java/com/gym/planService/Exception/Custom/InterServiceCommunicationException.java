package com.gym.planService.Exception.Custom;

public class InterServiceCommunicationException extends RuntimeException {
    public InterServiceCommunicationException(String message) {
        super(message);
    }
}
