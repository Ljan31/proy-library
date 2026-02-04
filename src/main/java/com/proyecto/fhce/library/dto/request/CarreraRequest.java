package com.proyecto.fhce.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CarreraRequest {
  @NotBlank(message = "Nombre de carrera es requerido")
  @Size(max = 200)
  private String nombre_carrera;

  @NotBlank(message = "Código de carrera es requerido")
  @Size(max = 20)
  private String codigo_carrera;

  public String getNombre_carrera() {
    return nombre_carrera;
  }

  public void setNombre_carrera(String nombre_carrera) {
    this.nombre_carrera = nombre_carrera;
  }

  public String getCodigo_carrera() {
    return codigo_carrera;
  }

  public void setCodigo_carrera(String codigo_carrera) {
    this.codigo_carrera = codigo_carrera;
  }

}