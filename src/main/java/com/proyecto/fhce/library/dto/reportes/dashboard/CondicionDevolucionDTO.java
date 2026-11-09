package com.proyecto.fhce.library.dto.reportes.dashboard;

public class CondicionDevolucionDTO {
  private String condicion;
  private Long cantidad;

  public CondicionDevolucionDTO() {
  }

  public CondicionDevolucionDTO(String condicion, Long cantidad) {
    this.condicion = condicion;
    this.cantidad = cantidad;
  }

  public String getCondicion() {
    return condicion;
  }

  public void setCondicion(String condicion) {
    this.condicion = condicion;
  }

  public Long getCantidad() {
    return cantidad;
  }

  public void setCantidad(Long cantidad) {
    this.cantidad = cantidad;
  }

}
