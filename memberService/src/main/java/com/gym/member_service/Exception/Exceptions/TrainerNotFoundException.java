package com.gym.member_service.Exception.Exceptions;

public class TrainerNotFoundException extends RuntimeException {
  public TrainerNotFoundException(String message) {
    super(message);
  }
}
