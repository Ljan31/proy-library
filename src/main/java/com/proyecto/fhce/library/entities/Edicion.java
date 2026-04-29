package com.proyecto.fhce.library.entities;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "ediciones")
public class Edicion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long idEdicion;

  @Column(unique = true, length = 20)
  private String isbn;

  @Column(length = 200)
  private String editorial;

  @Column(name = "ano_publicacion")
  private Integer anoPublicacion;

  @Column(length = 50)
  private String edicion;

  @Column(length = 500)
  private String imagenPortada;

  @Column(name = "pdf_url", length = 500)
  private String pdfUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "libro_id", nullable = false)
  private Libro libro;

  @OneToMany(mappedBy = "edicion", cascade = CascadeType.ALL)
  private List<Ejemplar> ejemplares;

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

  public String getImagenPortada() {
    return imagenPortada;
  }

  public void setImagenPortada(String imagenPortada) {
    this.imagenPortada = imagenPortada;
  }

  public Libro getLibro() {
    return libro;
  }

  public void setLibro(Libro libro) {
    this.libro = libro;
  }

  public List<Ejemplar> getEjemplares() {
    return ejemplares;
  }

  public void setEjemplares(List<Ejemplar> ejemplares) {
    this.ejemplares = ejemplares;
  }

  public String getPdfUrl() {
    return pdfUrl;
  }

  public void setPdfUrl(String pdfUrl) {
    this.pdfUrl = pdfUrl;
  }

}