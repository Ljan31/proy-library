package com.proyecto.fhce.library.dto.reportes.dashboard;

public class PrestamoEstadoDTO {
  private String estado;
  private Long cantidad;

  public PrestamoEstadoDTO(String estado, Long cantidad) {
    this.estado = estado;
    this.cantidad = cantidad;
  }

  public PrestamoEstadoDTO() {
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
    this.estado = estado;
  }

  public Long getCantidad() {
    return cantidad;
  }

  public void setCantidad(Long cantidad) {
    this.cantidad = cantidad;
  }

}
