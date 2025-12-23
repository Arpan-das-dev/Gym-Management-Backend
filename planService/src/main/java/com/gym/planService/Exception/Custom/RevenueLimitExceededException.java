package com.gym.planService.Exception.Custom;

public class RevenueLimitExceededException extends RuntimeException {
  public RevenueLimitExceededException(String message) {
    super(message);
  }
}
