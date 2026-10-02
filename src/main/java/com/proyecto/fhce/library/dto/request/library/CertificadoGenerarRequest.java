package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.constraints.NotNull;

public class CertificadoGenerarRequest {

  @NotNull
  private Long usuarioId;

  @NotNull
  private Long bibliotecaId;

  // Si false, genera pero no persiste el PDF
  private boolean guardarPdf = false;
}