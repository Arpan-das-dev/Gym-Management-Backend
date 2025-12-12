package com.gym.adminservice.Exceptions.Custom;

public class UnauthorizedRequestException extends RuntimeException {
  public UnauthorizedRequestException(String message) {
    super(message);
  }
}
