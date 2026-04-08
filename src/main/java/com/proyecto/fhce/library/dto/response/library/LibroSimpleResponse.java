package com.proyecto.fhce.library.dto.response.library;

public class LibroSimpleResponse {
  private Long idLibro;
  private String isbn;
  private String titulo;
  private String autores; // concatenado
  private String editorial;
  private Integer anoPublicacion;
  private String imagen_portada;

  public Long getIdLibro() {
    return idLibro;
  }

  public void setIdLibro(Long idLibro) {
    this.idLibro = idLibro;
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

  public String getAutores() {
    return autores;
  }

  public void setAutores(String autores) {
    this.autores = autores;
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

  public String getImagen_portada() {
    return imagen_portada;
  }

  public void setImagen_portada(String imagen_portada) {
    this.imagen_portada = imagen_portada;
  }

}
