package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detalle completo de una categoría")
public class CategoriaDetalleResponse extends CategoriaLibroResponse {
  @Schema(description = "Libros más recientes de la categoría")
  private List<LibroSimpleResponse> librosRecientes;

  // Getters y Setters
  public List<LibroSimpleResponse> getLibrosRecientes() {
    return librosRecientes;
  }

  public void setLibrosRecientes(List<LibroSimpleResponse> librosRecientes) {
    this.librosRecientes = librosRecientes;
  }
}