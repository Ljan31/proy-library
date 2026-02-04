package com.proyecto.fhce.library.dto.response;

public class BibliotecaSimpleResponse {
  private Long id_biblioteca;
  private String nombre;

  public Long getId_biblioteca() {
    return id_biblioteca;
  }

  public void setId_biblioteca(Long id_biblioteca) {
    this.id_biblioteca = id_biblioteca;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

}
