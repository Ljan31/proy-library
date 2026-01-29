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

  // private EstadoCarrera estado;
}