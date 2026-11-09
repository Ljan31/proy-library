package com.proyecto.fhce.library.dto.reportes.dashboard;

public class LibroMasPrestadoDashboardDTO {
  private Long idLibro;
  private String titulo;
  private Long cantidad;

  public LibroMasPrestadoDashboardDTO(Long idLibro, String titulo, Long cantidad) {
    this.idLibro = idLibro;
    this.titulo = titulo;
    this.cantidad = cantidad;
  }

  public LibroMasPrestadoDashboardDTO() {
  }

  public Long getIdLibro() {
    return idLibro;
  }

  public void setIdLibro(Long idLibro) {
    this.idLibro = idLibro;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public Long getCantidad() {
    return cantidad;
  }

  public void setCantidad(Long cantidad) {
    this.cantidad = cantidad;
  }

}
