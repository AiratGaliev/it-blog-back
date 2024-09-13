package com.github.airatgaliev.itblogback.exception;

public class EmailAlreadyConfirmedException extends RuntimeException {

  public EmailAlreadyConfirmedException(String message) {
    super(message);
  }
}
