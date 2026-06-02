package com.proyecto.fhce.library.dto.reportes.inventario;

public class EstadoEjemplarBibliotecaDTO {
  private String biblioteca;

  private Long disponibles;

  private Long prestados;

  private Long reservados;

  private Long reparacion;

  private Long perdidos;

  private Long danados;

  private Long deteriorados;

  private Long bajas;

  public EstadoEjemplarBibliotecaDTO() {
  }

  public EstadoEjemplarBibliotecaDTO(
      String biblioteca,
      Long disponibles,
      Long prestados,
      Long reservados,
      Long reparacion,
      Long perdidos,
      Long danados,
      Long deteriorados,
      Long bajas) {

    this.biblioteca = biblioteca;
    this.disponibles = disponibles;
    this.prestados = prestados;
    this.reservados = reservados;
    this.reparacion = reparacion;
    this.perdidos = perdidos;
    this.danados = danados;
    this.deteriorados = deteriorados;
    this.bajas = bajas;
  }

  public String getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(String biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Long getDisponibles() {
    return disponibles;
  }

  public void setDisponibles(Long disponibles) {
    this.disponibles = disponibles;
  }

  public Long getPrestados() {
    return prestados;
  }

  public void setPrestados(Long prestados) {
    this.prestados = prestados;
  }

  public Long getReservados() {
    return reservados;
  }

  public void setReservados(Long reservados) {
    this.reservados = reservados;
  }

  public Long getReparacion() {
    return reparacion;
  }

  public void setReparacion(Long reparacion) {
    this.reparacion = reparacion;
  }

  public Long getPerdidos() {
    return perdidos;
  }

  public void setPerdidos(Long perdidos) {
    this.perdidos = perdidos;
  }

  public Long getDanados() {
    return danados;
  }

  public void setDanados(Long danados) {
    this.danados = danados;
  }

  public Long getDeteriorados() {
    return deteriorados;
  }

  public void setDeteriorados(Long deteriorados) {
    this.deteriorados = deteriorados;
  }

  public Long getBajas() {
    return bajas;
  }

  public void setBajas(Long bajas) {
    this.bajas = bajas;
  }

}
