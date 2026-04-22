package com.proyecto.fhce.library.exception;

public class SancionException {

  /** Ya existe una sanción activa para este préstamo */
  public static class SancionDuplicadaException extends RuntimeException {
    public SancionDuplicadaException(Long idPrestamo) {
      super("Ya existe una sanción activa para el préstamo con id: " + idPrestamo +
          ". No se puede generar una sanción duplicada.");
    }
  }

  /** La sanción no existe */
  public static class SancionNotFoundException extends RuntimeException {
    public SancionNotFoundException(Long idSancion) {
      super("Sanción no encontrada con id: " + idSancion);
    }
  }

  /** Transición de estado inválida */
  public static class EstadoInvalidoException extends RuntimeException {
    public EstadoInvalidoException(String estadoActual, String estadoDestino) {
      super("No se puede cambiar el estado de '" + estadoActual +
          "' a '" + estadoDestino + "'. La transición no está permitida.");
    }
  }

  /** Operación sobre una sanción que no está activa */
  public static class SancionNoActivaException extends RuntimeException {
    public SancionNoActivaException(Long idSancion, String estadoActual) {
      super("La sanción " + idSancion + " no puede ser modificada porque su estado es: " + estadoActual);
    }
  }

  /** El préstamo no corresponde a un estado vencido */
  public static class PrestamoNoVencidoException extends RuntimeException {
    public PrestamoNoVencidoException(Long idPrestamo) {
      super("El préstamo " + idPrestamo + " no tiene días de retraso. No se puede generar sanción.");
    }
  }
}