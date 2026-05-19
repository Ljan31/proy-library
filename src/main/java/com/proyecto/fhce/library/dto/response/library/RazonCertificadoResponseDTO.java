package com.proyecto.fhce.library.dto.response.library;

public class RazonCertificadoResponseDTO {
  private Long idRazon;
  private Long bibliotecaId;
  private String nombre;
  private String descripcion;
  private String requisitos; // JSON string
  private Boolean activo;

  // getters and setters

  public Long getIdRazon() {
    return idRazon;
  }

  public void setIdRazon(Long idRazon) {
    this.idRazon = idRazon;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getRequisitos() {
    return requisitos;
  }

  public void setRequisitos(String requisitos) {
    this.requisitos = requisitos;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }
}
