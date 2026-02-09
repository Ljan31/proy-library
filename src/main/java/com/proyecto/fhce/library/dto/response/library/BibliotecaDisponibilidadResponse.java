package com.proyecto.fhce.library.dto.response.library;

public class BibliotecaDisponibilidadResponse {
  private BibliotecaSimpleResponse biblioteca;
  private Integer ejemplaresDisponibles;
  private Integer ejemplaresTotal;
  private Integer reservasPendientes;

  public BibliotecaSimpleResponse getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(BibliotecaSimpleResponse biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Integer getEjemplaresDisponibles() {
    return ejemplaresDisponibles;
  }

  public void setEjemplaresDisponibles(Integer ejemplaresDisponibles) {
    this.ejemplaresDisponibles = ejemplaresDisponibles;
  }

  public Integer getEjemplaresTotal() {
    return ejemplaresTotal;
  }

  public void setEjemplaresTotal(Integer ejemplaresTotal) {
    this.ejemplaresTotal = ejemplaresTotal;
  }

  public Integer getReservasPendientes() {
    return reservasPendientes;
  }

  public void setReservasPendientes(Integer reservasPendientes) {
    this.reservasPendientes = reservasPendientes;
  }

}
