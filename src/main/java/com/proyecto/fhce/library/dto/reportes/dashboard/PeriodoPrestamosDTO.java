package com.proyecto.fhce.library.dto.reportes.dashboard;

public class PeriodoPrestamosDTO {
  private Integer anio;
  private Integer mes;
  private Long cantidad;

  public PeriodoPrestamosDTO() {
  }

  public PeriodoPrestamosDTO(Integer anio, Integer mes, Long cantidad) {
    this.anio = anio;
    this.mes = mes;
    this.cantidad = cantidad;
  }

  public Integer getAnio() {
    return anio;
  }

  public void setAnio(Integer anio) {
    this.anio = anio;
  }

  public Integer getMes() {
    return mes;
  }

  public void setMes(Integer mes) {
    this.mes = mes;
  }

  public Long getCantidad() {
    return cantidad;
  }

  public void setCantidad(Long cantidad) {
    this.cantidad = cantidad;
  }

}
