package com.proyecto.fhce.library.mapper;

import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.dto.response.NotificacionResponse;
import com.proyecto.fhce.library.entities.Notificacion;

@Component
public class NotificacionMapper {

  public NotificacionResponse toResponse(Notificacion notificacion) {
    return new NotificacionResponse(
        notificacion.getIdNotificacion(),
        notificacion.getTipoNotificacion(),
        notificacion.getAsunto(),
        notificacion.getMensaje(),
        notificacion.getFechaEnvio(),
        notificacion.getFechaLectura(),
        notificacion.getEstadoEnvio(),
        notificacion.getCanal(),
        notificacion.getIdReferencia(),
        notificacion.fueLeida());
  }
}