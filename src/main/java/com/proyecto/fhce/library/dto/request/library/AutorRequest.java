package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AutorRequest {

  @NotBlank(message = "El nombre del autor es obligatorio")
  @Size(max = 200)
  private String nombre;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }
}
