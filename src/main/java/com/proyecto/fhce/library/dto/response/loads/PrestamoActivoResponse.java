package com.proyecto.fhce.library.dto.response.loads;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PrestamoActivoResponse {
  private Long id_prestamo;
  private LocalDateTime fechaPrestamo;
  private LocalDate fechaDevolucionEstimada;
  private Integer diasRestantes;
  private Boolean vencido;
  private Integer renovacionesRestantes;

  public Long getId_prestamo() {
    return id_prestamo;
  }

  public void setId_prestamo(Long id_prestamo) {
    this.id_prestamo = id_prestamo;
  }

  public LocalDateTime getFechaPrestamo() {
    return fechaPrestamo;
  }

  public void setFechaPrestamo(LocalDateTime fechaPrestamo) {
    this.fechaPrestamo = fechaPrestamo;
  }

  public LocalDate getFechaDevolucionEstimada() {
    return fechaDevolucionEstimada;
  }

  public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) {
    this.fechaDevolucionEstimada = fechaDevolucionEstimada;
  }

  public Integer getDiasRestantes() {
    return diasRestantes;
  }

  public void setDiasRestantes(Integer diasRestantes) {
    this.diasRestantes = diasRestantes;
  }

  public Boolean getVencido() {
    return vencido;
  }

  public void setVencido(Boolean vencido) {
    this.vencido = vencido;
  }

  public Integer getRenovacionesRestantes() {
    return renovacionesRestantes;
  }

  public void setRenovacionesRestantes(Integer renovacionesRestantes) {
    this.renovacionesRestantes = renovacionesRestantes;
  }

}
