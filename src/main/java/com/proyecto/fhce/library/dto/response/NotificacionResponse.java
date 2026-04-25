package com.proyecto.fhce.library.dto.response;

import java.time.LocalDateTime;

import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;
import com.proyecto.fhce.library.enums.notificaciones.EstadoEnvio;
import com.proyecto.fhce.library.enums.notificaciones.TipoNotificacion;

/**
 * Proyección segura de Notificacion para exponer en la API.
 * No expone campos internos como intentosEnvio, ultimoError ni tipoReferencia.
 */
public record NotificacionResponse(
    Long idNotificacion,
    TipoNotificacion tipoNotificacion,
    String asunto,
    String mensaje,
    LocalDateTime fechaEnvio,
    LocalDateTime fechaLectura,
    EstadoEnvio estadoEnvio,
    CanalNotificacion canal,
    Long idReferencia,
    boolean leida) {
}
