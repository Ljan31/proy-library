package com.proyecto.fhce.library.dto.request.loads;

import jakarta.validation.constraints.NotNull;

public class CertificadoRequest {
  private Long usuarioId;
  @NotNull(message = "El ID de la biblioteca es obligatorio")
  private Long bibliotecaId;
  private Integer diasValidez; // null = sin vencimiento
  private String nombres;
  private String apellidos;
  private String ci;

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

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getCi() {
    return ci;
  }

  public void setCi(String ci) {
    this.ci = ci;
  }

}