package com.proyecto.fhce.library.dto.response.library;

import java.time.LocalDateTime;

public class FormatoCertificadoResponse {
  private Long idFormato;
  private Long bibliotecaId;
  private String nombreBiblioteca;
  private String url;
  private String nombreArchivo;
  private String firmaUrl;
  private String selloUrl;
  private Integer version;
  private Boolean activo;
  private String subidoPor;
  private LocalDateTime fechaCreacion;
  private LocalDateTime fechaActualizacion;
  private String cargoResponsable;
  private String nombreResponsable;

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

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getFirmaUrl() {
    return firmaUrl;
  }

  public void setFirmaUrl(String firmaUrl) {
    this.firmaUrl = firmaUrl;
  }

  public String getSelloUrl() {
    return selloUrl;
  }

  public void setSelloUrl(String selloUrl) {
    this.selloUrl = selloUrl;
  }

  public String getCargoResponsable() {
    return cargoResponsable;
  }

  public void setCargoResponsable(String cargoResponsable) {
    this.cargoResponsable = cargoResponsable;
  }

  public String getNombreResponsable() {
    return nombreResponsable;
  }

  public void setNombreResponsable(String nombreResponsable) {
    this.nombreResponsable = nombreResponsable;
  }
}
