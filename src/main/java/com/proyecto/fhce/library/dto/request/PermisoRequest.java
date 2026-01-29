package com.proyecto.fhce.library.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PermisoRequest {
  @NotBlank(message = "Nombre del permiso es requerido")
  private String nombre_permiso;

  private String descripcion;

  private String modulo;
}