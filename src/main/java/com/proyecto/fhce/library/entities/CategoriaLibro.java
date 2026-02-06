package com.proyecto.fhce.library.entities;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "book_categories")
public class CategoriaLibro {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id_categoria;

  @Column(nullable = false, length = 150)
  private String nombre_categoria;

  @Column(length = 500)
  private String descripcion;

  @Column(length = 20)
  private String codigo_dewey;

  @OneToMany(mappedBy = "categoria")
  private List<Libro> libros;

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

  public List<Libro> getLibros() {
    return libros;
  }

  public void setLibros(List<Libro> libros) {
    this.libros = libros;
  }

}