package com.proyecto.fhce.library.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

  // @ExceptionHandler(NoResourceFoundException.class)
  // public ResponseEntity<String> handleNoResource(NoResourceFoundException ex) {
  // return ResponseEntity.status(404)
  // .body("El recurso solicitado no existe o no está habilitado en este
  // profile");
  // }
}