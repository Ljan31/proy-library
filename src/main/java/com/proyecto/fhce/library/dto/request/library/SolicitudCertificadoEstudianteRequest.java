package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SolicitudCertificadoEstudianteRequest(

    @NotNull(message = "El ID de la biblioteca es obligatorio") Long bibliotecaId,

    @NotNull(message = "La razón es obligatoria") Long razonId, // ← Cambiar de Enum a Long (idRazon)

    @Size(max = 500, message = "La descripción no puede superar 500 caracteres") String descripcion) {
}