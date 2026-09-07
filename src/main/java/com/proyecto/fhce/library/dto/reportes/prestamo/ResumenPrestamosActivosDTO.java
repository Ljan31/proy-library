package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.time.LocalDate;

public class ResumenPrestamosActivosDTO {
  private LocalDate fechaReporte; // siempre LocalDate.now()
  private Long totalRegistros;
  private Long soloActivos; // estadoPrestamo == ACTIVO
  private Long renovados; // estadoPrestamo == RENOVADO
  private Long yaVencidos; // vencido == true (calculado)
  private Long vencenHoy; // fechaDevolucionEstimada == today
  // solo en reporte por-vencer:
  private Integer diasVentana; // el N que se pasó como parámetro

  public LocalDate getFechaReporte() {
    return fechaReporte;
  }

  public void setFechaReporte(LocalDate fechaReporte) {
    this.fechaReporte = fechaReporte;
  }

  public Long getTotalRegistros() {
    return totalRegistros;
  }

  public void setTotalRegistros(Long totalRegistros) {
    this.totalRegistros = totalRegistros;
  }

  public Long getSoloActivos() {
    return soloActivos;
  }

  public void setSoloActivos(Long soloActivos) {
    this.soloActivos = soloActivos;
  }

  public Long getRenovados() {
    return renovados;
  }

  public void setRenovados(Long renovados) {
    this.renovados = renovados;
  }

  public Long getYaVencidos() {
    return yaVencidos;
  }

  public void setYaVencidos(Long yaVencidos) {
    this.yaVencidos = yaVencidos;
  }

  public Long getVencenHoy() {
    return vencenHoy;
  }

  public void setVencenHoy(Long vencenHoy) {
    this.vencenHoy = vencenHoy;
  }

  public Integer getDiasVentana() {
    return diasVentana;
  }

  public void setDiasVentana(Integer diasVentana) {
    this.diasVentana = diasVentana;
  }

}