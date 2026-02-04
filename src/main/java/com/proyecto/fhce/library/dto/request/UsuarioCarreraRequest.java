package com.proyecto.fhce.library.dto.request;

import jakarta.validation.constraints.NotNull;

public class UsuarioCarreraRequest {
  @NotNull(message = "Usuario ID es requerido")
  private Long usuarioId;

  @NotNull(message = "Carrera ID es requerido")
  private Long carreraId;

  private String matricula;

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Long getCarreraId() {
    return carreraId;
  }

  public void setCarreraId(Long carreraId) {
    this.carreraId = carreraId;
  }

  public String getMatricula() {
    return matricula;
  }

  public void setMatricula(String matricula) {
    this.matricula = matricula;
  }

}
