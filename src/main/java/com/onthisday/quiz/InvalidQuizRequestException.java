package com.onthisday.quiz;

public class InvalidQuizRequestException extends RuntimeException {

  public InvalidQuizRequestException(String message) {
    super(message);
  }
}
