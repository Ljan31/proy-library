package com.proyecto.fhce.library.dto.request.loads;

import jakarta.validation.constraints.NotNull;

public class CertificadoRequest {
  @NotNull(message = "Usuario ID es requerido")
  private Long usuarioId;

  private Integer diasValidez; // null = sin vencimiento

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Integer getDiasValidez() {
    return diasValidez;
  }

  public void setDiasValidez(Integer diasValidez) {
    this.diasValidez = diasValidez;
  }

}