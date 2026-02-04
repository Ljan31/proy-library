package com.proyecto.fhce.library.dto.response;

public class CarreraSimpleResponse {
  private Long id_carrera;
  private String nombre_carrera;
  private String codigo_carrera;
  private String matricula;

  public Long getId_carrera() {
    return id_carrera;
  }

  public void setId_carrera(Long id_carrera) {
    this.id_carrera = id_carrera;
  }

  public String getNombre_carrera() {
    return nombre_carrera;
  }

  public void setNombre_carrera(String nombre_carrera) {
    this.nombre_carrera = nombre_carrera;
  }

  public String getCodigo_carrera() {
    return codigo_carrera;
  }

  public void setCodigo_carrera(String codigo_carrera) {
    this.codigo_carrera = codigo_carrera;
  }

  public String getMatricula() {
    return matricula;
  }

  public void setMatricula(String matricula) {
    this.matricula = matricula;
  }

}
