package com.proyecto.fhce.library.dto.response.library;

import java.time.LocalDateTime;

public class FormatoCertificadoResponse {
  private Long idFormato;
  private Long bibliotecaId;
  private String nombreBiblioteca;
  private String nombreArchivo;
  private Integer version;
  private Boolean activo;
  private String subidoPor;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;

  public Long getIdFormato() {
    return idFormato;
  }

  public void setIdFormato(Long idFormato) {
    this.idFormato = idFormato;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getNombreBiblioteca() {
    return nombreBiblioteca;
  }

  public void setNombreBiblioteca(String nombreBiblioteca) {
    this.nombreBiblioteca = nombreBiblioteca;
  }

  public String getNombreArchivo() {
    return nombreArchivo;
  }

  public void setNombreArchivo(String nombreArchivo) {
    this.nombreArchivo = nombreArchivo;
  }

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

  public String getSubidoPor() {
    return subidoPor;
  }

  public void setSubidoPor(String subidoPor) {
    this.subidoPor = subidoPor;
  }

  public LocalDateTime getFechaCreacion() {
    return fechaCreacion;
  }

  public void setFechaCreacion(LocalDateTime fechaCreacion) {
    this.fechaCreacion = fechaCreacion;
  }

  public LocalDateTime getFechaActualizacion() {
    return fechaActualizacion;
  }

  public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
    this.fechaActualizacion = fechaActualizacion;
  }
}
