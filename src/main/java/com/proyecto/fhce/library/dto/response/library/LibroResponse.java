package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

public class LibroResponse {
  private Long id_libro;
  private String isbn;
  private String titulo;
  private String editorial;
  private Integer anoPublicacion;
  private String edicion;
  private Integer numero_paginas;
  private String idioma;
  private CategoriaLibroResponse categoria;
  private String descripcion;
  private String imagen_portada;
  // private Set<AutorResponse> autores;
  private Integer ejemplaresTotal;
  private Integer ejemplaresDisponibles;
  private List<BibliotecaDisponibilidadResponse> disponibilidadPorBiblioteca;

  public Long getId_libro() {
    return id_libro;
  }

  public void setId_libro(Long id_libro) {
    this.id_libro = id_libro;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getEditorial() {
    return editorial;
  }

  public void setEditorial(String editorial) {
    this.editorial = editorial;
  }

  public Integer getAnoPublicacion() {
    return anoPublicacion;
  }

  public void setAnoPublicacion(Integer anoPublicacion) {
    this.anoPublicacion = anoPublicacion;
  }

  public String getEdicion() {
    return edicion;
  }

  public void setEdicion(String edicion) {
    this.edicion = edicion;
  }

  public Integer getNumero_paginas() {
    return numero_paginas;
  }

  public void setNumero_paginas(Integer numero_paginas) {
    this.numero_paginas = numero_paginas;
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

  public String getImagen_portada() {
    return imagen_portada;
  }

  public void setImagen_portada(String imagen_portada) {
    this.imagen_portada = imagen_portada;
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

}
