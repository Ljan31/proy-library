package com.proyecto.fhce.library.dto.response.library;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría con conteo de libros")
public class CategoriaConConteoResponse {
  @Schema(description = "ID de la categoría")
  private Long id_categoria;

  @Schema(description = "Nombre de la categoría")
  private String nombre_categoria;

  @Schema(description = "Cantidad de libros")
  private Long cantidadLibros;

  // Getters y Setters
  public Long getId_categoria() {
    return id_categoria;
  }

  public void setId_categoria(Long id_categoria) {
    this.id_categoria = id_categoria;
  }

  public String getNombre_categoria() {
    return nombre_categoria;
  }

  public void setNombre_categoria(String nombre_categoria) {
    this.nombre_categoria = nombre_categoria;
  }

  public Long getCantidadLibros() {
    return cantidadLibros;
  }

  public void setCantidadLibros(Long cantidadLibros) {
    this.cantidadLibros = cantidadLibros;
  }
}