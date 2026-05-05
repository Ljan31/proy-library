package com.proyecto.fhce.library.enums;

/**
 * Estados posibles de una reserva en el sistema SIGEB.
 *
 * Flujo normal:
 * ACTIVA → NOTIFICADA → ATENDIDA
 *
 * Flujos de cancelación:
 * ACTIVA → CANCELADA (el usuario cancela manualmente)
 * NOTIFICADA → VENCIDA (el CRON detecta que no recogió en tiempo)
 * NOTIFICADA → CANCELADA (cancelación manual con ejemplar ya asignado)
 */
public enum EstadoReserva {

  ACTIVA,
  NOTIFICADA,
  ATENDIDA,

  /**
   * Cancelada por el usuario o por el sistema (ej: usuario recibió sanción).
   * Si había un ejemplar asignado, debe liberarse (estado → DISPONIBLE).
   */
  CANCELADA,
  VENCIDA
}