package com.proyecto.fhce.library.dto.reportes;

public class InventarioBibliotecaDTO {

  private String biblioteca;

  private Long totalEjemplares;
  private Long disponibles;
  private Long prestados;
  private Long reservados;
  private Long enReparacion;
  private Long deteriorados;
  private Long perdidos;
  private Long bajas;

  public InventarioBibliotecaDTO() {
  }

  public String getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(String biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Long getTotalEjemplares() {
    return totalEjemplares;
  }

  public void setTotalEjemplares(Long totalEjemplares) {
    this.totalEjemplares = totalEjemplares;
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

  public Long getEnReparacion() {
    return enReparacion;
  }

  public void setEnReparacion(Long enReparacion) {
    this.enReparacion = enReparacion;
  }

  public Long getDeteriorados() {
    return deteriorados;
  }

  public void setDeteriorados(Long deteriorados) {
    this.deteriorados = deteriorados;
  }

  public Long getPerdidos() {
    return perdidos;
  }

  public void setPerdidos(Long perdidos) {
    this.perdidos = perdidos;
  }

  public Long getBajas() {
    return bajas;
  }

  public void setBajas(Long bajas) {
    this.bajas = bajas;
  }
}