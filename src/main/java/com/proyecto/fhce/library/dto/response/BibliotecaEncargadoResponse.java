package com.proyecto.fhce.library.dto.response;

import java.time.LocalDate;

import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.enums.RolEncargado;

public class BibliotecaEncargadoResponse {
  private Long id;
  private UsuarioSimpleResponse usuario;
  private RolEncargado rolEncargado;
  private LocalDate fechaAsignacion;
  private Boolean activo;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UsuarioSimpleResponse getUsuario() {
    return usuario;
  }

  public void setUsuario(UsuarioSimpleResponse usuario) {
    this.usuario = usuario;
  }

  public RolEncargado getRolEncargado() {
    return rolEncargado;
  }

  public void setRolEncargado(RolEncargado rolEncargado) {
    this.rolEncargado = rolEncargado;
  }

  public LocalDate getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDate fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

}
