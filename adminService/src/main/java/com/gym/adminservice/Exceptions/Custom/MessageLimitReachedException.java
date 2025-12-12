package com.gym.adminservice.Exceptions.Custom;

public class MessageLimitReachedException extends RuntimeException {
  public MessageLimitReachedException(String message) {
    super(message);
  }
}
