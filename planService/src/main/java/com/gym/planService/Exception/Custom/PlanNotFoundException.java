package com.gym.planService.Exception.Custom;

public class PlanNotFoundException extends RuntimeException {
  public PlanNotFoundException(String message) {
    super(message);
  }
}
