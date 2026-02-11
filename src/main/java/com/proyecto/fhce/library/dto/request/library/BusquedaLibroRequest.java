package com.proyecto.fhce.library.dto.request.library;

public class BusquedaLibroRequest {
  private String titulo;
  private String autor;
  private String isbn;
  private String editorial;
  private Integer anoPublicacion;
  private Long categoriaId;
  private Long bibliotecaId;

  // private EstadoEjemplar estadoDisponibilidad;
  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getAutor() {
    return autor;
  }

  public void setAutor(String autor) {
    this.autor = autor;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
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

  public Long getCategoriaId() {
    return categoriaId;
  }

  public void setCategoriaId(Long categoriaId) {
    this.categoriaId = categoriaId;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

}
