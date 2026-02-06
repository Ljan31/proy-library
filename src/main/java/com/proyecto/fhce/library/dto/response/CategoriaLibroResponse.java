package com.proyecto.fhce.library.dto.response;

public class CategoriaLibroResponse {
  private Long id_categoria;
  private String nombre_categoria;
  private String descripcion;
  private String codigo_dewey;
  private Integer librosCount;

  public Long getId_categoria() {
    return id_categoria;
  }

  public void setId_categoria(Long id_categoria) {
    this.id_categoria = id_categoria;
  }

  public String getNombre_categoria() {
    return nombre_categoria;
  }

  public void setNombre_categoria(String nombre_categoria) {
    this.nombre_categoria = nombre_categoria;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getCodigo_dewey() {
    return codigo_dewey;
  }

  public void setCodigo_dewey(String codigo_dewey) {
    this.codigo_dewey = codigo_dewey;
  }

  public Integer getLibrosCount() {
    return librosCount;
  }

  public void setLibrosCount(Integer librosCount) {
    this.librosCount = librosCount;
  }

}
