package com.proyecto.fhce.library.entities;

import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Libro {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, nullable = false)
  private Long id_libro;

  @Column(unique = true, length = 20)
  private String isbn;

  @Column(nullable = false, length = 500)
  private String titulo;

  @Column(length = 200)
  private String editorial;

  @Column(name = "ano_publicacion")
  private Integer anoPublicacion;

  @Column(length = 50)
  private String edicion;

  private Integer numero_paginas;

  @Column(length = 50)
  private String idioma;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private CategoriaLibro categoria;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @Column(length = 500)
  private String imagen_portada;

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

  public CategoriaLibro getCategoria() {
    return categoria;
  }

  public void setCategoria(CategoriaLibro categoria) {
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

  // @ManyToMany
  // @JoinTable(
  // name = "books_authors",
  // joinColumns = @JoinColumn(name = "book_id"),
  // inverseJoinColumns = @JoinColumn(name = "author_id")
  // )
  // private Set<Autor> autores;

  // @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL)
  // private List<Ejemplar> ejemplares;

  // @OneToMany(mappedBy = "libro")
  // private List<Reserva> reservas;

}
