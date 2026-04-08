package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

public class LibroResponse {
  private Long idLibro;
  private String titulo;
  private String idioma;
  private String descripcion;
  private CategoriaLibroResponse categoria;

  // ✅ Lista de ediciones disponibles (antes era un solo ISBN)
  private List<EdicionResponse> ediciones;
  // private Set<AutorResponse> autores;
  private Integer ejemplaresTotal;
  private Integer ejemplaresDisponibles;
  private List<BibliotecaDisponibilidadResponse> disponibilidadPorBiblioteca;

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getIdioma() {
    return idioma;
  }

  public void setIdioma(String idioma) {
    this.idioma = idioma;
  }

  public CategoriaLibroResponse getCategoria() {
    return categoria;
  }

  public void setCategoria(CategoriaLibroResponse categoria) {
    this.categoria = categoria;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Integer getEjemplaresTotal() {
    return ejemplaresTotal;
  }

  public void setEjemplaresTotal(Integer ejemplaresTotal) {
    this.ejemplaresTotal = ejemplaresTotal;
  }

  public Integer getEjemplaresDisponibles() {
    return ejemplaresDisponibles;
  }

  public void setEjemplaresDisponibles(Integer ejemplaresDisponibles) {
    this.ejemplaresDisponibles = ejemplaresDisponibles;
  }

  public List<BibliotecaDisponibilidadResponse> getDisponibilidadPorBiblioteca() {
    return disponibilidadPorBiblioteca;
  }

  public void setDisponibilidadPorBiblioteca(List<BibliotecaDisponibilidadResponse> disponibilidadPorBiblioteca) {
    this.disponibilidadPorBiblioteca = disponibilidadPorBiblioteca;
  }

  public Long getIdLibro() {
    return idLibro;
  }

  public void setIdLibro(Long idLibro) {
    this.idLibro = idLibro;
  }

  public List<EdicionResponse> getEdiciones() {
    return ediciones;
  }

  public void setEdiciones(List<EdicionResponse> ediciones) {
    this.ediciones = ediciones;
  }

}
