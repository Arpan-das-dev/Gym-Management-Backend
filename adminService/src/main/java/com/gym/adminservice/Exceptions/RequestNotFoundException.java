package com.gym.adminservice.Exceptions;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(String message){
        super(message);
    }
}
