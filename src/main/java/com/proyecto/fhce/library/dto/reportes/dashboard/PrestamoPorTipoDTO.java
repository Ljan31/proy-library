package com.proyecto.fhce.library.dto.reportes.dashboard;

public class PrestamoPorTipoDTO {
  private String tipo;
  private Long cantidad;

  public PrestamoPorTipoDTO(String tipo, Long cantidad) {
    this.tipo = tipo;
    this.cantidad = cantidad;
  }

  public PrestamoPorTipoDTO() {
  }

  public String getTipo() {
    return tipo;
  }

  public void setTipo(String tipo) {
    this.tipo = tipo;
  }

  public Long getCantidad() {
    return cantidad;
  }

  public void setCantidad(Long cantidad) {
    this.cantidad = cantidad;
  }

}
