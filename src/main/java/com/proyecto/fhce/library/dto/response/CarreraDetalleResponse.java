package com.proyecto.fhce.library.dto.response;

import java.util.List;

public class CarreraDetalleResponse {
  private Long id_carrera;
  private String nombre_carrera;
  private String codigo_carrera;
  private Integer estudiantesActivos;
  private List<BibliotecaSimpleResponse> bibliotecas;
  private Integer bibliotecasCount;

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

  public List<BibliotecaSimpleResponse> getBibliotecas() {
    return bibliotecas;
  }

  public void setBibliotecas(List<BibliotecaSimpleResponse> bibliotecas) {
    this.bibliotecas = bibliotecas;
  }

  public Integer getBibliotecasCount() {
    return bibliotecasCount;
  }

  public void setBibliotecasCount(Integer bibliotecasCount) {
    this.bibliotecasCount = bibliotecasCount;
  }

  public Integer getEstudiantesActivos() {
    return estudiantesActivos;
  }

  public void setEstudiantesActivos(Integer estudiantesActivos) {
    this.estudiantesActivos = estudiantesActivos;
  }

}
