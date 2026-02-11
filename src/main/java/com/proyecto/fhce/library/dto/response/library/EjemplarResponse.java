package com.proyecto.fhce.library.dto.response.library;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.proyecto.fhce.library.dto.response.loads.PrestamoActivoResponse;
import com.proyecto.fhce.library.enums.EstadoEjemplar;

public class EjemplarResponse {
  private Long id_ejemplar;
  private LibroSimpleResponse libro;
  private BibliotecaSimpleResponse biblioteca;
  private String codigo_ejemplar;
  private String codigo_topografico;
  private String ubicacion_fisica;
  private EstadoEjemplar estadoEjemplar;
  private LocalDate fechaAdquisicion;
  private BigDecimal precio_compra;
  private String observaciones;
  private PrestamoActivoResponse prestamoActivo; // si está prestado

  public Long getId_ejemplar() {
    return id_ejemplar;
  }

  public void setId_ejemplar(Long id_ejemplar) {
    this.id_ejemplar = id_ejemplar;
  }

  public LibroSimpleResponse getLibro() {
    return libro;
  }

  public void setLibro(LibroSimpleResponse libro) {
    this.libro = libro;
  }

  public BibliotecaSimpleResponse getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(BibliotecaSimpleResponse biblioteca) {
    this.biblioteca = biblioteca;
  }

  public String getCodigo_ejemplar() {
    return codigo_ejemplar;
  }

  public void setCodigo_ejemplar(String codigo_ejemplar) {
    this.codigo_ejemplar = codigo_ejemplar;
  }

  public String getCodigo_topografico() {
    return codigo_topografico;
  }

  public void setCodigo_topografico(String codigo_topografico) {
    this.codigo_topografico = codigo_topografico;
  }

  public String getUbicacion_fisica() {
    return ubicacion_fisica;
  }

  public void setUbicacion_fisica(String ubicacion_fisica) {
    this.ubicacion_fisica = ubicacion_fisica;
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

  public BigDecimal getPrecio_compra() {
    return precio_compra;
  }

  public void setPrecio_compra(BigDecimal precio_compra) {
    this.precio_compra = precio_compra;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public PrestamoActivoResponse getPrestamoActivo() {
    return prestamoActivo;
  }

  public void setPrestamoActivo(PrestamoActivoResponse prestamoActivo) {
    this.prestamoActivo = prestamoActivo;
  }

}
