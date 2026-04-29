package com.proyecto.fhce.library.entities;

import java.util.ArrayList;
import java.util.List;

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
  @Column(unique = true, nullable = false, name = "id_libro")
  private Long idLibro;

  @Column(nullable = false, length = 500)
  private String titulo;

  @Column(length = 50)
  private String idioma;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private CategoriaLibro categoria;

  @Column(columnDefinition = "TEXT")
  private String descripcion;

  @OneToMany(mappedBy = "libro", cascade = CascadeType.ALL)
  private List<Edicion> ediciones;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "libros_autores", joinColumns = @JoinColumn(name = "libro_id"), inverseJoinColumns = @JoinColumn(name = "autor_id"))
  private List<Autor> autores = new ArrayList<>();

  public Long getId_libro() {
    return idLibro;
  }

  public void setId_libro(Long id_libro) {
    this.idLibro = id_libro;
  }

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

  public Long getIdLibro() {
    return idLibro;
  }

  public void setIdLibro(Long idLibro) {
    this.idLibro = idLibro;
  }

  public List<Edicion> getEdiciones() {
    return ediciones;
  }

  public void setEdiciones(List<Edicion> ediciones) {
    this.ediciones = ediciones;
  }

  public List<Autor> getAutores() {
    return autores;
  }

  public void setAutores(List<Autor> autores) {
    this.autores = autores;
  }

  // @OneToMany(mappedBy = "libro")
  // private List<Reserva> reservas;

}
