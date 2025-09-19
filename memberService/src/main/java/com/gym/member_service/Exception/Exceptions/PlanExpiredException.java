package com.gym.member_service.Exception.Exceptions;

public class PlanExpiredException extends RuntimeException {
  public PlanExpiredException(String message) {
    super(message);
  }
}
