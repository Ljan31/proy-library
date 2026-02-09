package com.proyecto.fhce.library.dto.response.library;

import com.proyecto.fhce.library.enums.TipoBiblioteca;

public class BibliotecaSimpleResponse {
  private Long id_biblioteca;
  private String nombre;
  private TipoBiblioteca tipoBiblioteca;

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

  public TipoBiblioteca getTipoBiblioteca() {
    return tipoBiblioteca;
  }

  public void setTipoBiblioteca(TipoBiblioteca tipoBiblioteca) {
    this.tipoBiblioteca = tipoBiblioteca;
  }

}
