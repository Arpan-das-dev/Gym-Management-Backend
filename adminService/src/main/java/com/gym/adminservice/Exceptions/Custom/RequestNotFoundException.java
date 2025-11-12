package com.gym.adminservice.Exceptions.Custom;

public class RequestNotFoundException extends RuntimeException {
    public RequestNotFoundException(String message){
        super(message);
    }
}
