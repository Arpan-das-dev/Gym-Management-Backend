package com.gym.member_service.Exception.Exceptions;

public class TrainerAlreadyExistsException extends RuntimeException {
    public TrainerAlreadyExistsException(String message){
        super(message);
    }
}
