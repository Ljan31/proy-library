package com.proyecto.fhce.library.dto.response;

import java.time.LocalDate;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

public class EjemplarAuditoriaResponse {

  private Long idEjemplar;
  private String codigoEjemplar;
  private String codigoTopografico;
  private String clasificacionDecimal;
  private String cutterAutor;
  private String cutterTitulo;
  private String ubicacionFisica;
  private EstadoEjemplar estadoEjemplar;
  private LocalDate fechaAdquisicion;
  private String observaciones;

  private Long idEdicion;
  private Long idBiblioteca;

  public Long getIdEjemplar() {
    return idEjemplar;
  }

  public void setIdEjemplar(Long idEjemplar) {
    this.idEjemplar = idEjemplar;
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

  public String getClasificacionDecimal() {
    return clasificacionDecimal;
  }

  public void setClasificacionDecimal(String clasificacionDecimal) {
    this.clasificacionDecimal = clasificacionDecimal;
  }

  public String getCutterAutor() {
    return cutterAutor;
  }

  public void setCutterAutor(String cutterAutor) {
    this.cutterAutor = cutterAutor;
  }

  public String getCutterTitulo() {
    return cutterTitulo;
  }

  public void setCutterTitulo(String cutterTitulo) {
    this.cutterTitulo = cutterTitulo;
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

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public Long getIdEdicion() {
    return idEdicion;
  }

  public void setIdEdicion(Long idEdicion) {
    this.idEdicion = idEdicion;
  }

  public Long getIdBiblioteca() {
    return idBiblioteca;
  }

  public void setIdBiblioteca(Long idBiblioteca) {
    this.idBiblioteca = idBiblioteca;
  }

}