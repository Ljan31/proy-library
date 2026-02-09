package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Estadísticas de categorías")
public class EstadisticasCategoriasResponse {
  @Schema(description = "Total de categorías")
  private Integer totalCategorias;

  @Schema(description = "Categorías sin libros")
  private Integer categoriasSinLibros;

  @Schema(description = "Top 10 categorías con más libros")
  private List<CategoriaConConteoResponse> categoriasConMasLibros;

  // Getters y Setters
  public Integer getTotalCategorias() {
    return totalCategorias;
  }

  public void setTotalCategorias(Integer totalCategorias) {
    this.totalCategorias = totalCategorias;
  }

  public Integer getCategoriasSinLibros() {
    return categoriasSinLibros;
  }

  public void setCategoriasSinLibros(Integer categoriasSinLibros) {
    this.categoriasSinLibros = categoriasSinLibros;
  }

  public List<CategoriaConConteoResponse> getCategoriasConMasLibros() {
    return categoriasConMasLibros;
  }

  public void setCategoriasConMasLibros(List<CategoriaConConteoResponse> categoriasConMasLibros) {
    this.categoriasConMasLibros = categoriasConMasLibros;
  }
}