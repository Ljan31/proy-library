package com.proyecto.fhce.library.dto.request.users;

import jakarta.validation.constraints.NotNull;

public class UsuarioCarreraRequestEst {

  @NotNull(message = "Carrera ID es requerido")
  private Long carreraId;

  private String matricula;

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