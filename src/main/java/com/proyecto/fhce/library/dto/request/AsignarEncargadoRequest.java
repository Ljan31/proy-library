package com.proyecto.fhce.library.dto.request;

import com.proyecto.fhce.library.enums.RolEncargado;

import jakarta.validation.constraints.NotNull;

public class AsignarEncargadoRequest {
  @NotNull
  private Long usuarioId;

  @NotNull
  private RolEncargado rolEncargado;

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public RolEncargado getRolEncargado() {
    return rolEncargado;
  }

  public void setRolEncargado(RolEncargado rolEncargado) {
    this.rolEncargado = rolEncargado;
  }

}
