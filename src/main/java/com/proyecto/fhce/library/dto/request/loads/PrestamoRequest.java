package com.proyecto.fhce.library.dto.request.loads;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public class PrestamoRequest {
  @NotNull(message = "Ejemplar ID es requerido")
  private Long ejemplarId;

  @NotNull(message = "Usuario ID es requerido")
  private Long usuarioId;

  @NotNull(message = "Biblioteca ID es requerido")
  private Long bibliotecaId;

  @Future(message = "Fecha de devolución debe ser futura")
  private LocalDate fechaDevolucionEstimada;

  private String observaciones;

  public Long getEjemplarId() {
    return ejemplarId;
  }

  public void setEjemplarId(Long ejemplarId) {
    this.ejemplarId = ejemplarId;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public LocalDate getFechaDevolucionEstimada() {
    return fechaDevolucionEstimada;
  }

  public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) {
    this.fechaDevolucionEstimada = fechaDevolucionEstimada;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

}
