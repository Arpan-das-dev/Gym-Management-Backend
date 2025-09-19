package com.gym.member_service.Exception.Exceptions;

public class InvalidSessionException extends RuntimeException {
  public InvalidSessionException(String message) {
    super(message);
  }
}
