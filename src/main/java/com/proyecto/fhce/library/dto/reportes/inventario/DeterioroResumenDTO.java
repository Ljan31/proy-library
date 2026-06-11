package com.proyecto.fhce.library.dto.reportes.inventario;

public class DeterioroResumenDTO {
  private Long totalDevoluciones; // devoluciones en el período
  private Long totalConDeterioro; // condicionDevolucion < condicionEntrega
  private Long sinCambio; // misma condición
  private Long mejorados; // condición mejoró (raro pero posible)
  private double porcentajeDeterioro; // totalConDeterioro / totalDevoluciones * 100

  public Long getTotalDevoluciones() {
    return totalDevoluciones;
  }

  public void setTotalDevoluciones(Long totalDevoluciones) {
    this.totalDevoluciones = totalDevoluciones;
  }

  public Long getTotalConDeterioro() {
    return totalConDeterioro;
  }

  public void setTotalConDeterioro(Long totalConDeterioro) {
    this.totalConDeterioro = totalConDeterioro;
  }

  public Long getSinCambio() {
    return sinCambio;
  }

  public void setSinCambio(Long sinCambio) {
    this.sinCambio = sinCambio;
  }

  public Long getMejorados() {
    return mejorados;
  }

  public void setMejorados(Long mejorados) {
    this.mejorados = mejorados;
  }

  public double getPorcentajeDeterioro() {
    return porcentajeDeterioro;
  }

  public void setPorcentajeDeterioro(double porcentajeDeterioro) {
    this.porcentajeDeterioro = porcentajeDeterioro;
  }

}