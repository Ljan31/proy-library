package com.proyecto.fhce.library.dto.request;

import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;
import com.proyecto.fhce.library.enums.notificaciones.TipoNotificacion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO usado internamente por otros módulos para solicitar la creación
 * de una notificación. El canal puede omitirse si el servicio lo resolverá
 * desde la configuración del usuario.
 */
public record CrearNotificacionRequest(

    @NotNull(message = "El ID de usuario es obligatorio") Long idUsuario,

    @NotNull(message = "El tipo de notificación es obligatorio") TipoNotificacion tipoNotificacion,

    @NotNull(message = "El asunto es obligatorio") @Size(max = 200, message = "El asunto no puede superar 200 caracteres") String asunto,

    @NotNull(message = "El mensaje es obligatorio") String mensaje,

    // Canal puede ser null: el servicio lo resuelve desde preferencias del usuario
    CanalNotificacion canal,

    Long idReferencia,

    // PRESTAMO | RESERVA | SANCION
    String tipoReferencia) {
}
