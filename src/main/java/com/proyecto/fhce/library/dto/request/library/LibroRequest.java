package com.proyecto.fhce.library.dto.request.library;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LibroRequest {

  @NotBlank(message = "El título es obligatorio")
  @Size(max = 500)
  private String titulo;

  @Size(max = 50)
  private String idioma;

  private Long categoriaId;
  private List<Long> autorIds;
  private String descripcion;

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

  public List<Long> getAutorIds() {
    return autorIds;
  }

  public void setAutorIds(List<Long> autorIds) {
    this.autorIds = autorIds;
  }

}