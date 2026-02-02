package com.proyecto.fhce.library.dto.request;

import jakarta.validation.constraints.NotBlank;

public class PermisoRequest {
  @NotBlank(message = "Nombre del permiso es requerido")
  private String nombre_permiso;

  private String descripcion;

  private String modulo;

  public String getNombre_permiso() {
    return nombre_permiso;
  }

  public void setNombre_permiso(String nombre_permiso) {
    this.nombre_permiso = nombre_permiso;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getModulo() {
    return modulo;
  }

  public void setModulo(String modulo) {
    this.modulo = modulo;
  }

}