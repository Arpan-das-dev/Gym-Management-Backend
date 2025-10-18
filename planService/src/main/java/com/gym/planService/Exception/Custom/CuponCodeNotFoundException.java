package com.gym.planService.Exception.Custom;

public class CuponCodeNotFoundException extends RuntimeException {
  public CuponCodeNotFoundException(String message) {
    super(message);
  }
}
