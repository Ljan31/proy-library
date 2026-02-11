package com.proyecto.fhce.library.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "ejemplares")
public class Ejemplar {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(unique = true, nullable = false, name = "id_ejemplar")
  private Long idEjemplar;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id", nullable = false)
  private Libro libro;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "library_id", nullable = false)
  private Biblioteca biblioteca;

  @Column(unique = true, nullable = false, length = 50, name = "codigo_ejemplar")
  private String codigoEjemplar;

  @Column(length = 100, name = "codigo_topografico")
  private String codigoTopografico;

  @Column(length = 200, name = "ubicacion_fisica")
  private String ubicacionFisica;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_ejemplar", nullable = false)
  private EstadoEjemplar estadoEjemplar;

  @Column(name = "fecha_adquisicion")
  private LocalDate fechaAdquisicion;

  @Column(precision = 10, scale = 2, name = "precio_compra")
  private BigDecimal precioCompra;

  @Column(columnDefinition = "TEXT")
  private String observaciones;

  // @OneToMany(mappedBy = "ejemplar")
  // private List<Prestamo> prestamos;

  @OneToMany(mappedBy = "ejemplar", cascade = CascadeType.ALL)
  private List<HistorialEstadoEjemplar> historialEstados;

  @PrePersist
  protected void onCreate() {
    if (estadoEjemplar == null)
      estadoEjemplar = EstadoEjemplar.DISPONIBLE;
  }

  public Long getIdEjemplar() {
    return idEjemplar;
  }

  public void setIdEjemplar(Long idEjemplar) {
    this.idEjemplar = idEjemplar;
  }

  public Libro getLibro() {
    return libro;
  }

  public void setLibro(Libro libro) {
    this.libro = libro;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
    this.biblioteca = biblioteca;
  }

  public String getCodigoEjemplar() {
    return codigoEjemplar;
  }

  public void setCodigoEjemplar(String codigoEjemplar) {
    this.codigoEjemplar = codigoEjemplar;
  }

  public String getCodigoTopografico() {
    return codigoTopografico;
  }

  public void setCodigoTopografico(String codigoTopografico) {
    this.codigoTopografico = codigoTopografico;
  }

  public String getUbicacionFisica() {
    return ubicacionFisica;
  }

  public void setUbicacionFisica(String ubicacionFisica) {
    this.ubicacionFisica = ubicacionFisica;
  }

  public EstadoEjemplar getEstadoEjemplar() {
    return estadoEjemplar;
  }

  public void setEstadoEjemplar(EstadoEjemplar estadoEjemplar) {
    this.estadoEjemplar = estadoEjemplar;
  }

  public LocalDate getFechaAdquisicion() {
    return fechaAdquisicion;
  }

  public void setFechaAdquisicion(LocalDate fechaAdquisicion) {
    this.fechaAdquisicion = fechaAdquisicion;
  }

  public BigDecimal getPrecioCompra() {
    return precioCompra;
  }

  public void setPrecioCompra(BigDecimal precioCompra) {
    this.precioCompra = precioCompra;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public List<HistorialEstadoEjemplar> getHistorialEstados() {
    return historialEstados;
  }

  public void setHistorialEstados(List<HistorialEstadoEjemplar> historialEstados) {
    this.historialEstados = historialEstados;
  }

}