package com.proyecto.fhce.library.exception;

public class BusinessException extends RuntimeException {
  public BusinessException(String message) {
    super(message);
  }
}