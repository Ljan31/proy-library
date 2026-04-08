package com.proyecto.fhce.library.dto.response.library;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.proyecto.fhce.library.dto.response.loads.PrestamoActivoResponse;
import com.proyecto.fhce.library.enums.EstadoEjemplar;

public class EjemplarResponse {
  private Long id_ejemplar;
  private String codigoEjemplar;
  private String codigoTopografico;
  private String ubicacionFisica;
  private EstadoEjemplar estadoEjemplar;
  private LocalDate fechaAdquisicion;
  private BigDecimal precioCompra;
  private String observaciones;

  private EdicionSimpleResponse edicion;
  private BibliotecaSimpleResponse biblioteca;
  private PrestamoActivoResponse prestamoActivo; // si está prestado

  public Long getId_ejemplar() {
    return id_ejemplar;
  }

  public void setId_ejemplar(Long id_ejemplar) {
    this.id_ejemplar = id_ejemplar;
  }

  public BibliotecaSimpleResponse getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(BibliotecaSimpleResponse biblioteca) {
    this.biblioteca = biblioteca;
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

  public BigDecimal getPrecioCompra() {
    return precioCompra;
  }

  public void setPrecioCompra(BigDecimal precioCompra) {
    this.precioCompra = precioCompra;
  }

  public EdicionSimpleResponse getEdicion() {
    return edicion;
  }

  public void setEdicion(EdicionSimpleResponse edicion) {
    this.edicion = edicion;
  }

}
