package it.vitalegi.rpgboard.be.security;

public class InvalidTokenException extends RuntimeException {
  public InvalidTokenException(String message) {
    super(message);
  }

  public InvalidTokenException(String message, Throwable cause) {
    super(message, cause);
  }

  public InvalidTokenException(Throwable cause) {
    super(cause);
  }

  protected InvalidTokenException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
