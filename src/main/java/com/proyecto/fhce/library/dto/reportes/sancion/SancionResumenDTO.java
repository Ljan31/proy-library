package com.proyecto.fhce.library.dto.reportes.sancion;

import java.math.BigDecimal;

public class SancionResumenDTO {
  private Long totalSanciones;
  private Long activas;
  private Long pagadas;
  private Long condonadas;
  private Long multas;
  private Long suspensiones;
  private BigDecimal montoTotalGenerado;
  private BigDecimal montoTotalPagado;
  private BigDecimal montoPendiente;

  public Long getTotalSanciones() {
    return totalSanciones;
  }

  public void setTotalSanciones(Long totalSanciones) {
    this.totalSanciones = totalSanciones;
  }

  public Long getActivas() {
    return activas;
  }

  public void setActivas(Long activas) {
    this.activas = activas;
  }

  public Long getPagadas() {
    return pagadas;
  }

  public void setPagadas(Long pagadas) {
    this.pagadas = pagadas;
  }

  public Long getCondonadas() {
    return condonadas;
  }

  public void setCondonadas(Long condonadas) {
    this.condonadas = condonadas;
  }

  public Long getMultas() {
    return multas;
  }

  public void setMultas(Long multas) {
    this.multas = multas;
  }

  public Long getSuspensiones() {
    return suspensiones;
  }

  public void setSuspensiones(Long suspensiones) {
    this.suspensiones = suspensiones;
  }

  public BigDecimal getMontoTotalGenerado() {
    return montoTotalGenerado;
  }

  public void setMontoTotalGenerado(BigDecimal montoTotalGenerado) {
    this.montoTotalGenerado = montoTotalGenerado;
  }

  public BigDecimal getMontoTotalPagado() {
    return montoTotalPagado;
  }

  public void setMontoTotalPagado(BigDecimal montoTotalPagado) {
    this.montoTotalPagado = montoTotalPagado;
  }

  public BigDecimal getMontoPendiente() {
    return montoPendiente;
  }

  public void setMontoPendiente(BigDecimal montoPendiente) {
    this.montoPendiente = montoPendiente;
  }

}