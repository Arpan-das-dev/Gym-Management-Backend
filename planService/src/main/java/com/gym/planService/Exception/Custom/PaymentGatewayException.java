package com.gym.planService.Exception.Custom;

public class PaymentGatewayException extends RuntimeException {
  public PaymentGatewayException(String message) {
    super(message);
  }
}
