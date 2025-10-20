package com.gym.planService.Exception.Custom;

public class InvalidPlanPriceException extends RuntimeException {
  public InvalidPlanPriceException(String message) {
    super(message);
  }
}
