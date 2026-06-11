package com.proyecto.fhce.library.dto.reportes.prestamo;

public class ResumenHistorialDTO {
  private Long totalPrestamos;
  private Long totalDevueltos;
  private Long totalVencidos;
  private Long totalRenovados;
  private Long totalConDeterioro;
  private Long totalConRetraso;

  public Long getTotalPrestamos() {
    return totalPrestamos;
  }

  public void setTotalPrestamos(Long totalPrestamos) {
    this.totalPrestamos = totalPrestamos;
  }

  public Long getTotalDevueltos() {
    return totalDevueltos;
  }

  public void setTotalDevueltos(Long totalDevueltos) {
    this.totalDevueltos = totalDevueltos;
  }

  public Long getTotalVencidos() {
    return totalVencidos;
  }

  public void setTotalVencidos(Long totalVencidos) {
    this.totalVencidos = totalVencidos;
  }

  public Long getTotalRenovados() {
    return totalRenovados;
  }

  public void setTotalRenovados(Long totalRenovados) {
    this.totalRenovados = totalRenovados;
  }

  public Long getTotalConDeterioro() {
    return totalConDeterioro;
  }

  public void setTotalConDeterioro(Long totalConDeterioro) {
    this.totalConDeterioro = totalConDeterioro;
  }

  public Long getTotalConRetraso() {
    return totalConRetraso;
  }

  public void setTotalConRetraso(Long totalConRetraso) {
    this.totalConRetraso = totalConRetraso;
  }

}
