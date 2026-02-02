package com.proyecto.fhce.library.dto.response;

public class PermisoResponse {
  private Long id_permiso;
  private String nombre_permiso;
  private String descripcion;
  private String modulo;

  public Long getId_permiso() {
    return id_permiso;
  }

  public void setId_permiso(Long id_permiso) {
    this.id_permiso = id_permiso;
  }

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
