package com.proyecto.fhce.library.dto.response.loads;

public record EstadoPrestamoUsuarioDTO(

    Long usuarioId,

    boolean tienePrestamosPendientes,

    long prestamosActivos,

    long prestamosVencidos,

    long prestamosRenovados

) {
}