package com.gym.trainerService.Exception;

public class NoTrainerFoundException extends RuntimeException{
    public NoTrainerFoundException(String message) {
        super(message);
    }
}
