package com.gym.member_service.Exception.Exceptions;

public class UnAuthorizedException extends RuntimeException {
  public UnAuthorizedException(String message) {
    super(message);
  }
}
