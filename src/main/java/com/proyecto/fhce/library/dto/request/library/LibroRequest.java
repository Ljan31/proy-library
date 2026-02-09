package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LibroRequest {
  @Size(max = 20, message = "ISBN no puede exceder 20 caracteres")
  private String isbn;

  @NotBlank(message = "Título es requerido")
  @Size(max = 500)
  private String titulo;

  @Size(max = 200)
  private String editorial;

  @Min(value = 1000, message = "Año de publicación inválido")
  @Max(value = 2100, message = "Año de publicación inválido")
  private Integer anoPublicacion;

  private String edicion;

  @Min(value = 1, message = "Número de páginas debe ser mayor a 0")
  private Integer numero_paginas;

  private String idioma;

  private Long categoriaId;

  private String descripcion;

  private String imagen_portada;

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

  public Long getCategoriaId() {
    return categoriaId;
  }

  public void setCategoriaId(Long categoriaId) {
    this.categoriaId = categoriaId;
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

  // @NotEmpty(message = "Debe especificar al menos un autor")
  // private Set<Long> autorIds;

}
