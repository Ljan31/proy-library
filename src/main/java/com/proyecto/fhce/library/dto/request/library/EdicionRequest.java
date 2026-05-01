package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EdicionRequest {

  // @NotBlank(message = "El ISBN es obligatorio")
  // @Size(max = 20)
  private String isbn;

  // @NotBlank(message = "La editorial es obligatoria")
  // @Size(max = 200)
  private String editorial;

  // @NotNull(message = "El año de publicación es obligatorio")
  private Integer anoPublicacion;

  @Size(max = 50)
  private String edicion;

  private String imagenPortada;

  private String pdfUrl;

  @NotNull(message = "El libro es obligatorio")
  private Long libroId;

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

  public String getImagenPortada() {
    return imagenPortada;
  }

  public void setImagenPortada(String imagenPortada) {
    this.imagenPortada = imagenPortada;
  }

  public Long getLibroId() {
    return libroId;
  }

  public void setLibroId(Long libroId) {
    this.libroId = libroId;
  }

  public String getPdfUrl() {
    return pdfUrl;
  }

  public void setPdfUrl(String pdfUrl) {
    this.pdfUrl = pdfUrl;
  }

}