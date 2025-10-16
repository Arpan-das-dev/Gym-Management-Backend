package com.gym.planService.Exception.Custom;

public class DuplicatePLanFoundException extends RuntimeException {
  public DuplicatePLanFoundException(String message) {
    super(message);
  }
}
