package com.proyecto.fhce.library.dto.response.library;

public class LibroMasPrestadoResponse {
  private String titulo;
  private String autores;
  private Long cantidadPrestamos;

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getAutores() {
    return autores;
  }

  public void setAutores(String autores) {
    this.autores = autores;
  }

  public Long getCantidadPrestamos() {
    return cantidadPrestamos;
  }

  public void setCantidadPrestamos(Long cantidadPrestamos) {
    this.cantidadPrestamos = cantidadPrestamos;
  }

}
