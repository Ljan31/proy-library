package com.proyecto.fhce.library.exception;

import org.springframework.http.HttpStatus;

public class NotificacionException extends RuntimeException {

  private final HttpStatus status;

  public NotificacionException(String mensaje) {
    this(mensaje, HttpStatus.BAD_REQUEST);
  }

  public NotificacionException(String mensaje, HttpStatus status) {
    super(mensaje);
    this.status = status;
  }

  public NotificacionException(String mensaje, Throwable causa) {
    super(mensaje, causa);
    this.status = HttpStatus.INTERNAL_SERVER_ERROR;
  }

  /** Código HTTP con el que debe responder la API para este error. */
  public HttpStatus getStatus() {
    return status;
  }

  // -------------------------------------------------------
  // Fábricas estáticas — evitan strings mágicos dispersos
  // -------------------------------------------------------

  public static NotificacionException noEncontrada(Long id) {
    return new NotificacionException(
        "Notificación no encontrada con id: " + id, HttpStatus.NOT_FOUND);
  }

  public static NotificacionException accesoDenegado(Long idNotificacion, Long idUsuario) {
    return new NotificacionException(
        "El usuario " + idUsuario + " no tiene acceso a la notificación " + idNotificacion,
        HttpStatus.FORBIDDEN);
  }

  public static NotificacionException duplicada(Long idUsuario, String tipo) {
    return new NotificacionException(
        "Ya existe una notificación activa de tipo " + tipo + " para el usuario " + idUsuario,
        HttpStatus.CONFLICT);
  }

  public static NotificacionException envioFallido(String canal, String motivo) {
    return new NotificacionException(
        "Fallo al enviar notificación por canal " + canal + ": " + motivo,
        HttpStatus.BAD_GATEWAY);
  }
}
