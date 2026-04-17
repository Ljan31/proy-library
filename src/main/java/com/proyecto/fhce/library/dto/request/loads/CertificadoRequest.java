package com.proyecto.fhce.library.dto.request.loads;

import jakarta.validation.constraints.NotNull;

public class CertificadoRequest {
  @NotNull(message = "Usuario ID es requerido")
  private Long usuarioId;
  @NotNull(message = "El ID de la biblioteca es obligatorio")
  private Long bibliotecaId;
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

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

}