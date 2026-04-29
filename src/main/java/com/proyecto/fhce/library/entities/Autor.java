package com.proyecto.fhce.library.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "autores")
public class Autor {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_autor")
  private Long idAutor;

  @Column(nullable = false, length = 200)
  private String nombre;

  @ManyToMany(mappedBy = "autores")
  private List<Libro> libros = new ArrayList<>();

  // Getters y setters
  public Long getIdAutor() {
    return idAutor;
  }

  public void setIdAutor(Long idAutor) {
    this.idAutor = idAutor;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public List<Libro> getLibros() {
    return libros;
  }

  public void setLibros(List<Libro> libros) {
    this.libros = libros;
  }
}