package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

public class EstadisticasBibliotecaResponse {
  private BibliotecaSimpleResponse biblioteca;
  private Integer totalLibros;
  private Integer totalEjemplares;
  private Integer ejemplaresDisponibles;
  private Integer ejemplaresPrestados;
  private Integer prestamosActivos;
  private Integer prestamosVencidos;
  private Integer reservasPendientes;
  private Integer usuariosActivos;
  private List<LibroMasPrestadoResponse> librosMasPrestados;

  public BibliotecaSimpleResponse getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(BibliotecaSimpleResponse biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Integer getTotalLibros() {
    return totalLibros;
  }

  public void setTotalLibros(Integer totalLibros) {
    this.totalLibros = totalLibros;
  }

  public Integer getTotalEjemplares() {
    return totalEjemplares;
  }

  public void setTotalEjemplares(Integer totalEjemplares) {
    this.totalEjemplares = totalEjemplares;
  }

  public Integer getEjemplaresDisponibles() {
    return ejemplaresDisponibles;
  }

  public void setEjemplaresDisponibles(Integer ejemplaresDisponibles) {
    this.ejemplaresDisponibles = ejemplaresDisponibles;
  }

  public Integer getEjemplaresPrestados() {
    return ejemplaresPrestados;
  }

  public void setEjemplaresPrestados(Integer ejemplaresPrestados) {
    this.ejemplaresPrestados = ejemplaresPrestados;
  }

  public Integer getPrestamosActivos() {
    return prestamosActivos;
  }

  public void setPrestamosActivos(Integer prestamosActivos) {
    this.prestamosActivos = prestamosActivos;
  }

  public Integer getPrestamosVencidos() {
    return prestamosVencidos;
  }

  public void setPrestamosVencidos(Integer prestamosVencidos) {
    this.prestamosVencidos = prestamosVencidos;
  }

  public Integer getReservasPendientes() {
    return reservasPendientes;
  }

  public void setReservasPendientes(Integer reservasPendientes) {
    this.reservasPendientes = reservasPendientes;
  }

  public Integer getUsuariosActivos() {
    return usuariosActivos;
  }

  public void setUsuariosActivos(Integer usuariosActivos) {
    this.usuariosActivos = usuariosActivos;
  }

  public List<LibroMasPrestadoResponse> getLibrosMasPrestados() {
    return librosMasPrestados;
  }

  public void setLibrosMasPrestados(List<LibroMasPrestadoResponse> librosMasPrestados) {
    this.librosMasPrestados = librosMasPrestados;
  }

}
