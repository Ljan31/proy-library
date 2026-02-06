package com.proyecto.fhce.library.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoriaLibroRequest {
  @NotBlank(message = "Nombre de categoría es requerido")
  @Size(max = 150)
  private String nombre_categoria;

  private String descripcion;

  @Size(max = 20)
  private String codigo_dewey;

  public String getNombre_categoria() {
    return nombre_categoria;
  }

  public void setNombre_categoria(String nombre_categoria) {
    this.nombre_categoria = nombre_categoria;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getCodigo_dewey() {
    return codigo_dewey;
  }

  public void setCodigo_dewey(String codigo_dewey) {
    this.codigo_dewey = codigo_dewey;
  }

}
