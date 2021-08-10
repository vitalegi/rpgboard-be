package it.vitalegi.rpgboard.be.exception;

public class MethodNotAllowedException extends RuntimeException {
  public MethodNotAllowedException(String message) {
    super(message);
  }
}
