package com.proyecto.fhce.library.dto.response.library;

import java.time.LocalDateTime;

public record SolicitudCertificadoResponse(

    Long idSolicitud,

    Long usuarioId,
    String nombreEstudiante,
    String carnet,

    Long bibliotecaId,
    String nombreBiblioteca,

    Long razonId,
    String nombreRazon,
    String requisitos,

    String descripcion,

    String estado,
    LocalDateTime fechaSolicitud,
    LocalDateTime fechaUltimaActualizacion) {
}