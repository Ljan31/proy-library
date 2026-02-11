package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

public class DisponibilidadLibroResponse {
  private Long libroId;
  private String tituloLibro;

  private int totalEjemplares;
  private int ejemplaresDisponibles;
  private int ejemplaresPrestados;
  private int ejemplaresReservados;

  private boolean hayDisponibles;

  private List<DisponibilidadPorBibliotecaResponse> porBiblioteca;

  public Long getLibroId() {
    return libroId;
  }

  public void setLibroId(Long libroId) {
    this.libroId = libroId;
  }

  public String getTituloLibro() {
    return tituloLibro;
  }

  public void setTituloLibro(String tituloLibro) {
    this.tituloLibro = tituloLibro;
  }

  public int getTotalEjemplares() {
    return totalEjemplares;
  }

  public void setTotalEjemplares(int totalEjemplares) {
    this.totalEjemplares = totalEjemplares;
  }

  public int getEjemplaresDisponibles() {
    return ejemplaresDisponibles;
  }

  public void setEjemplaresDisponibles(int ejemplaresDisponibles) {
    this.ejemplaresDisponibles = ejemplaresDisponibles;
  }

  public int getEjemplaresPrestados() {
    return ejemplaresPrestados;
  }

  public void setEjemplaresPrestados(int ejemplaresPrestados) {
    this.ejemplaresPrestados = ejemplaresPrestados;
  }

  public int getEjemplaresReservados() {
    return ejemplaresReservados;
  }

  public void setEjemplaresReservados(int ejemplaresReservados) {
    this.ejemplaresReservados = ejemplaresReservados;
  }

  public boolean isHayDisponibles() {
    return hayDisponibles;
  }

  public void setHayDisponibles(boolean hayDisponibles) {
    this.hayDisponibles = hayDisponibles;
  }

  public List<DisponibilidadPorBibliotecaResponse> getPorBiblioteca() {
    return porBiblioteca;
  }

  public void setPorBiblioteca(List<DisponibilidadPorBibliotecaResponse> porBiblioteca) {
    this.porBiblioteca = porBiblioteca;
  }

}
