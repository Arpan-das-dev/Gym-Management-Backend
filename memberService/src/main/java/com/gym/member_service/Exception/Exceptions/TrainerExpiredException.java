package com.gym.member_service.Exception.Exceptions;

public class TrainerExpiredException extends RuntimeException {
    public TrainerExpiredException(String message){
        super(message);
    }
}
