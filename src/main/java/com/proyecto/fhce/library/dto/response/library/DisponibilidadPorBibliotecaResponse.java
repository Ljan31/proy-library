package com.proyecto.fhce.library.dto.response.library;

public class DisponibilidadPorBibliotecaResponse {
  private BibliotecaSimpleResponse biblioteca;

  private int total;
  private int disponibles;

  public BibliotecaSimpleResponse getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(BibliotecaSimpleResponse biblioteca) {
    this.biblioteca = biblioteca;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public int getDisponibles() {
    return disponibles;
  }

  public void setDisponibles(int disponibles) {
    this.disponibles = disponibles;
  }

}
