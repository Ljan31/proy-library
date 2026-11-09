package com.proyecto.fhce.library.dto.reportes.dashboard;

public class PrestamoPorBibliotecaDTO {
  private Long bibliotecaId;
  private String biblioteca;
  private Long cantidad;

  public PrestamoPorBibliotecaDTO(Long bibliotecaId, String biblioteca, Long cantidad) {
    this.bibliotecaId = bibliotecaId;
    this.biblioteca = biblioteca;
    this.cantidad = cantidad;
  }

  public PrestamoPorBibliotecaDTO() {
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(String biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Long getCantidad() {
    return cantidad;
  }

  public void setCantidad(Long cantidad) {
    this.cantidad = cantidad;
  }

}
