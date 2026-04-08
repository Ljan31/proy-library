package com.proyecto.fhce.library.dto.response.library;

public class EdicionResponse {

  private Long idEdicion;
  private String isbn;
  private String editorial;
  private Integer anoPublicacion;
  private String edicion;
  private Integer numeroPaginas;
  private String imagenPortada;

  // Info resumida del libro al que pertenece
  private LibroSimpleResponse libro;

  // Conteo de ejemplares (útil para el frontend)
  private int ejemplaresTotal;
  private int ejemplaresDisponibles;

  public Long getIdEdicion() {
    return idEdicion;
  }

  public void setIdEdicion(Long idEdicion) {
    this.idEdicion = idEdicion;
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

  public String getEdicion() {
    return edicion;
  }

  public void setEdicion(String edicion) {
    this.edicion = edicion;
  }

  public Integer getNumeroPaginas() {
    return numeroPaginas;
  }

  public void setNumeroPaginas(Integer numeroPaginas) {
    this.numeroPaginas = numeroPaginas;
  }

  public String getImagenPortada() {
    return imagenPortada;
  }

  public void setImagenPortada(String imagenPortada) {
    this.imagenPortada = imagenPortada;
  }

  public LibroSimpleResponse getLibro() {
    return libro;
  }

  public void setLibro(LibroSimpleResponse libro) {
    this.libro = libro;
  }

  public int getEjemplaresTotal() {
    return ejemplaresTotal;
  }

  public void setEjemplaresTotal(int ejemplaresTotal) {
    this.ejemplaresTotal = ejemplaresTotal;
  }

  public int getEjemplaresDisponibles() {
    return ejemplaresDisponibles;
  }

  public void setEjemplaresDisponibles(int ejemplaresDisponibles) {
    this.ejemplaresDisponibles = ejemplaresDisponibles;
  }

}