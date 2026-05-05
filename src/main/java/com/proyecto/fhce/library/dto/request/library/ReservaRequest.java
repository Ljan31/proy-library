package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.constraints.NotNull;

public class ReservaRequest {

  @NotNull(message = "El libro es obligatorio")
  private Long libroId;

  @NotNull(message = "La biblioteca es obligatoria")
  private Long bibliotecaId;

  private String observaciones;

  // Getters y Setters

  public Long getLibroId() {
    return libroId;
  }

  public void setLibroId(Long libroId) {
    this.libroId = libroId;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }
}
