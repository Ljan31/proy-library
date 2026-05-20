package com.proyecto.fhce.library.dto.request.library;

public class SolicitudCertificadoRequest {

  private Long bibliotecaId;

  private String nombres;

  private String apellidos;

  private String ci;

  private String matricula;

  private String razon;

  private String descripcion;

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

  public String getRazon() {
    return razon;
  }

  public void setRazon(String razon) {
    this.razon = razon;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

}
