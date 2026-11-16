package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SolicitudCertificadoRequest {

  @NotNull(message = "La biblioteca es obligatoria")
  private Long bibliotecaId;
  @NotBlank(message = "Los apellidos son obligatorios")
  private String nombres;
  @NotBlank(message = "Los apellidos son obligatorios")
  private String apellidos;
  @NotBlank(message = "El CI es obligatorio")
  private String ci;

  private String matricula;

  private Long razonCertificadoId;

  private String descripcion;

  private String email;

  private String telefono;

  public SolicitudCertificadoRequest() {
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

  public String getMatricula() {
    return matricula;
  }

  public void setMatricula(String matricula) {
    this.matricula = matricula;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public Long getRazonCertificadoId() {
    return razonCertificadoId;
  }

  public void setRazonCertificadoId(Long razonCertificadoId) {
    this.razonCertificadoId = razonCertificadoId;
  }

}
