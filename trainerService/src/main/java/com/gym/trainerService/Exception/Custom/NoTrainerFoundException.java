package com.gym.trainerService.Exception.Custom;

public class NoTrainerFoundException extends RuntimeException{
    public NoTrainerFoundException(String message) {
        super(message);
    }
}
