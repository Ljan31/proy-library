package com.proyecto.fhce.library.dto.request.loads;

import jakarta.validation.constraints.NotNull;

public class RenovacionRequest {
  @NotNull(message = "Préstamo ID es requerido")
  private Long prestamoId;

  private String motivo;

  public Long getPrestamoId() {
    return prestamoId;
  }

  public void setPrestamoId(Long prestamoId) {
    this.prestamoId = prestamoId;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }

}
