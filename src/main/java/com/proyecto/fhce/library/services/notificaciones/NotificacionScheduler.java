package com.proyecto.fhce.library.services.notificaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara el envío de las notificaciones pendientes y los reintentos.
 *
 * Los cron se configuran por properties. Para apagar el scheduler en un
 * entorno (desarrollo, tests): sigeb.notificaciones.scheduler.habilitado=false
 *
 * IMPORTANTE: pensado para UNA sola instancia del backend. Con dos instancias
 * corriendo a la vez, ambas tomarían las mismas pendientes y se enviarían
 * correos duplicados.
 */
@Component
@ConditionalOnProperty(name = "sigeb.notificaciones.scheduler.habilitado", havingValue = "true", matchIfMissing = true)
public class NotificacionScheduler {

  private static final Logger log = LoggerFactory.getLogger(NotificacionScheduler.class);

  private final NotificacionService notificacionService;

  public NotificacionScheduler(NotificacionService notificacionService) {
    this.notificacionService = notificacionService;
  }

  /** Envía las notificaciones PENDIENTE (por defecto, cada 5 minutos). */
  @Scheduled(cron = "${sigeb.scheduler.notificaciones.pendientes.cron:0 */5 * ** *}")
  public void procesarPendientes() {
    try {
      notificacionService.procesarPendientes();
    } catch (Exception e) {
      // Nunca dejar escapar la excepción: algunos contenedores cancelan el job.
      log.error("Error en el scheduler de pendientes: {}", e.getMessage(), e);
    }
  }

  /**
   * Reintenta las FALLIDO con intentos disponibles (por defecto, cada 30
   * minutos).
   */
  // @Scheduled(cron = "${sigeb.scheduler.notificaciones.reintentos.cron:0 */30 *
  // * * *}")
  public void procesarReintentos() {
    try {
      notificacionService.procesarReintentos();
    } catch (Exception e) {
      log.error("Error en el scheduler de reintentos: {}", e.getMessage(), e);
    }
  }
}
