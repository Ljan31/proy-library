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
  @Column(name = "id_categoria")
  private Long idCategoria;

  @Column(nullable = false, length = 150, name = "nombre_categoria")
  private String nombreCategoria;

  @Column(length = 500)
  private String descripcion;

  @Column(length = 20, name = "codigo_dewey")
  private String codigoDewey;

  @OneToMany(mappedBy = "categoria")
  private List<Libro> libros;

  public Long getId_categoria() {
    return idCategoria;
  }

  public void setId_categoria(Long id_categoria) {
    this.idCategoria = id_categoria;
  }

  public String getNombre_categoria() {
    return nombreCategoria;
  }

  public void setNombre_categoria(String nombre_categoria) {
    this.nombreCategoria = nombre_categoria;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getCodigo_dewey() {
    return codigoDewey;
  }

  public void setCodigo_dewey(String codigo_dewey) {
    this.codigoDewey = codigo_dewey;
  }

  public List<Libro> getLibros() {
    return libros;
  }

  public void setLibros(List<Libro> libros) {
    this.libros = libros;
  }

}