package com.proyecto.fhce.library.dto.response.users;

import java.time.LocalDateTime;

import com.proyecto.fhce.library.dto.response.CarreraSimpleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de asignación usuario-carrera")
public class UsuarioCarreraResponse {
  @Schema(description = "ID de la asignación")
  private Long id;

  @Schema(description = "Usuario asignado")
  private UsuarioSimpleResponse usuario;

  @Schema(description = "Carrera asignada")
  private CarreraSimpleResponse carrera;

  @Schema(description = "Matrícula del estudiante")
  private String matricula;

  @Schema(description = "Fecha de asignación")
  private LocalDateTime fechaAsignacion;

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

  public CarreraSimpleResponse getCarrera() {
    return carrera;
  }

  public void setCarrera(CarreraSimpleResponse carrera) {
    this.carrera = carrera;
  }

  public String getMatricula() {
    return matricula;
  }

  public void setMatricula(String matricula) {
    this.matricula = matricula;
  }

  public LocalDateTime getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }

}