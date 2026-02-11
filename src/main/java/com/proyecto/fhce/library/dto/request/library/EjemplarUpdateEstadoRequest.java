package com.proyecto.fhce.library.dto.request.library;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class EjemplarUpdateEstadoRequest {
  @NotNull(message = "Nuevo estado es requerido")
  private EstadoEjemplar nuevoEstado;

  @NotBlank(message = "Motivo del cambio es requerido")
  private String motivo;

  public EstadoEjemplar getNuevoEstado() {
    return nuevoEstado;
  }

  public void setNuevoEstado(EstadoEjemplar nuevoEstado) {
    this.nuevoEstado = nuevoEstado;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }

}
