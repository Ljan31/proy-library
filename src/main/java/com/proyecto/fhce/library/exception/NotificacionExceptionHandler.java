package com.proyecto.fhce.library.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce NotificacionException a su código HTTP (404, 409, 403...).
 *
 * Solo maneja esa excepción; no toca validaciones ni errores genéricos para no
 * pisar el manejo global que ya tenga el proyecto. HIGHEST_PRECEDENCE evita que
 * un handler global de Exception.class la capture antes. Si el proyecto ya tiene
 * su propio @RestControllerAdvice, puedes mover este método allí y borrar la clase.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NotificacionExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(NotificacionExceptionHandler.class);

  @ExceptionHandler(NotificacionException.class)
  public ResponseEntity<ErrorNotificacion> handle(NotificacionException ex) {
    HttpStatus status = ex.getStatus();
    log.warn("Error de notificaciones ({}): {}", status.value(), ex.getMessage());
    return ResponseEntity.status(status)
        .body(new ErrorNotificacion(status.value(), ex.getMessage(), LocalDateTime.now()));
  }

  public record ErrorNotificacion(int codigo, String mensaje, LocalDateTime timestamp) {
  }
}
