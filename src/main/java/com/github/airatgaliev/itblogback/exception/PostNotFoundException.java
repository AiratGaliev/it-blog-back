package com.github.airatgaliev.itblogback.exception;

public class PostNotFoundException extends RuntimeException {

  public PostNotFoundException(String message) {
    super(message);
  }
}
