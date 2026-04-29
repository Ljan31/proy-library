package com.proyecto.fhce.library.services.notificaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.proyecto.fhce.library.entities.Notificacion;
import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;

/**
 * Canal SISTEMA: la notificación ya está en BD. "Enviar" significa
 * marcarla lista para que el frontend la muestre en la bandeja.
 * fecha_lectura se actualiza cuando el usuario la abre (endpoint aparte).
 */
@Service
public class SistemaCanalService implements CanalEnvioService {

  private static final Logger log = LoggerFactory.getLogger(SistemaCanalService.class);

  @Override
  public CanalNotificacion canal() {
    return CanalNotificacion.SISTEMA;
  }

  @Override
  public void enviar(Notificacion notificacion) {
    log.debug("Notificación {} disponible en bandeja del usuario {}",
        notificacion.getIdNotificacion(), notificacion.getIdUsuario());
    // Extensión futura: WebSocket → simpMessagingTemplate.convertAndSendToUser(...)
  }
}