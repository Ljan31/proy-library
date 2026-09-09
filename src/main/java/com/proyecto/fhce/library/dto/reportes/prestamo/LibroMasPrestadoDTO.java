package com.proyecto.fhce.library.dto.reportes.prestamo;

public class LibroMasPrestadoDTO {
  private Long libroId;

  private String titulo;

  private Long cantidadPrestamos;

  public LibroMasPrestadoDTO(
      Long idLibro,
      String titulo,
      Long totalPrestamos) {
    this.libroId = idLibro;
    this.titulo = titulo;
    this.cantidadPrestamos = totalPrestamos;
  }

  public Long getLibroId() {
    return libroId;
  }

  public void setLibroId(Long libroId) {
    this.libroId = libroId;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public Long getCantidadPrestamos() {
    return cantidadPrestamos;
  }

  public void setCantidadPrestamos(Long cantidadPrestamos) {
    this.cantidadPrestamos = cantidadPrestamos;
  }

}
