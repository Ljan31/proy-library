package com.proyecto.fhce.library.dto.reportes.inventario;

public class InventarioResumenDTO {
  private Long totalEjemplares;

  private Long disponibles;

  private Long prestados;

  private Long reservados;

  private Long deteriorados;

  private Long perdidos;

  public InventarioResumenDTO() {
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

}
