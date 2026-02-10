package com.proyecto.fhce.library.dto.response.library;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría con conteo de libros")
public class CategoriaConConteoResponse {
  @Schema(description = "ID de la categoría")
  private Long idCategoria;

  @Schema(description = "Nombre de la categoría")
  private String nombreCategoria;

  @Schema(description = "Cantidad de libros")
  private Long cantidadLibros;

  public CategoriaConConteoResponse(Long idCategoria, String nombreCategoria, Long cantidadLibros) {
    this.idCategoria = idCategoria;
    this.nombreCategoria = nombreCategoria;
    this.cantidadLibros = cantidadLibros;
  }

  // Getters y Setters
  public Long getId_categoria() {
    return idCategoria;
  }

  public void setId_categoria(Long id_categoria) {
    this.idCategoria = id_categoria;
  }

  public String getNombre_categoria() {
    return nombreCategoria;
  }

  public void setNombre_categoria(String nombre_categoria) {
    this.nombreCategoria = nombre_categoria;
  }

  public Long getCantidadLibros() {
    return cantidadLibros;
  }

  public void setCantidadLibros(Long cantidadLibros) {
    this.cantidadLibros = cantidadLibros;
  }
}