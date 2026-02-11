package com.proyecto.fhce.library.dto.request.loads;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

import jakarta.validation.constraints.NotNull;

public class DevolucionRequest {
  @NotNull(message = "Préstamo ID es requerido")
  private Long prestamoId;

  private String observaciones;

  private EstadoEjemplar estadoEjemplar; // Por si viene dañado

  public Long getPrestamoId() {
    return prestamoId;
  }

  public void setPrestamoId(Long prestamoId) {
    this.prestamoId = prestamoId;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public EstadoEjemplar getEstadoEjemplar() {
    return estadoEjemplar;
  }

  public void setEstadoEjemplar(EstadoEjemplar estadoEjemplar) {
    this.estadoEjemplar = estadoEjemplar;
  }

}
