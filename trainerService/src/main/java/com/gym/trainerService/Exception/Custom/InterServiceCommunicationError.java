package com.gym.trainerService.Exception.Custom;

public class InterServiceCommunicationError extends RuntimeException {
    public InterServiceCommunicationError(String message) {
        super(message);
    }
}
