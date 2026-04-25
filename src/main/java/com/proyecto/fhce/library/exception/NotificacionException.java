package com.proyecto.fhce.library.exception;

public class NotificacionException extends RuntimeException {

  public NotificacionException(String mensaje) {
    super(mensaje);
  }

  public NotificacionException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }

  // -------------------------------------------------------
  // Fábricas estáticas — evitan strings mágicos dispersos
  // -------------------------------------------------------

  public static NotificacionException noEncontrada(Long id) {
    return new NotificacionException(
        "Notificación no encontrada con id: " + id);
  }

  public static NotificacionException accesoDenegado(Long idNotificacion, Long idUsuario) {
    return new NotificacionException(
        "El usuario " + idUsuario + " no tiene acceso a la notificación " + idNotificacion);
  }

  public static NotificacionException duplicada(Long idUsuario, String tipo) {
    return new NotificacionException(
        "Ya existe una notificación activa de tipo " + tipo + " para el usuario " + idUsuario);
  }

  public static NotificacionException envioFallido(String canal, String motivo) {
    return new NotificacionException(
        "Fallo al enviar notificación por canal " + canal + ": " + motivo);
  }
}