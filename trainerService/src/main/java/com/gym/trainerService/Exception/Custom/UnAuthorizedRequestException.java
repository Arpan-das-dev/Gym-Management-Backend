package com.gym.trainerService.Exception.Custom;

public class UnAuthorizedRequestException extends RuntimeException {
  public UnAuthorizedRequestException(String message) {
    super(message);
  }
}
