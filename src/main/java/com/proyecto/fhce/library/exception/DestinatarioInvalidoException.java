package com.proyecto.fhce.library.exception;

/**
 * El usuario no tiene un medio de contacto válido para el canal (sin correo,
 * correo mal formado, sin teléfono...). Es un error PERMANENTE: reintentar no
 * lo arregla, así que el servicio lo marca como FALLIDO definitivo.
 */
public class DestinatarioInvalidoException extends RuntimeException {

  public DestinatarioInvalidoException(String mensaje) {
    super(mensaje);
  }
}
