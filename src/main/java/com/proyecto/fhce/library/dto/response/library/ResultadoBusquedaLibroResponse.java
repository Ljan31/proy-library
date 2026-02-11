package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

public class ResultadoBusquedaLibroResponse {
  private List<LibroResponse> libros;
  private Integer totalResultados;
  private Integer paginaActual;
  private Integer totalPaginas;

  public List<LibroResponse> getLibros() {
    return libros;
  }

  public void setLibros(List<LibroResponse> libros) {
    this.libros = libros;
  }

  public Integer getTotalResultados() {
    return totalResultados;
  }

  public void setTotalResultados(Integer totalResultados) {
    this.totalResultados = totalResultados;
  }

  public Integer getPaginaActual() {
    return paginaActual;
  }

  public void setPaginaActual(Integer paginaActual) {
    this.paginaActual = paginaActual;
  }

  public Integer getTotalPaginas() {
    return totalPaginas;
  }

  public void setTotalPaginas(Integer totalPaginas) {
    this.totalPaginas = totalPaginas;
  }

}
