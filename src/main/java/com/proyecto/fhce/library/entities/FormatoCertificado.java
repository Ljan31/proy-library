package com.proyecto.fhce.library.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "formato_certificado")
public class FormatoCertificado {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_formato")
  private Long idFormato;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "biblioteca_id", nullable = false)
  private Biblioteca biblioteca;

  @Column(name = "nombre_archivo", nullable = false, length = 255)
  private String nombreArchivo;

  @Column(name = "ruta_archivo", nullable = false, length = 500)
  private String rutaArchivo;

  @Column(name = "firma_url", length = 500)
  private String firmaUrl;

  @Column(name = "sello_url", length = 500)
  private String selloUrl;

  @Column(nullable = false)
  private Integer version;

  @Column(nullable = false)
  private Boolean activo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subido_por")
  private Usuario subidoPor;

  @Column(name = "cargo_responsable", length = 20)
  private String cargoResponsable;

  @Column(name = "nombre_responsable", length = 150)
  private String nombreResponsable;

  @Column(name = "fecha_creacion", nullable = false)
  private LocalDateTime fechaCreacion;

  @Column(name = "fecha_actualizacion")
  private LocalDateTime fechaActualizacion;

  @PrePersist
  public void prePersist() {
    fechaCreacion = LocalDateTime.now();
    if (version == null)
      version = 1;
    if (activo == null)
      activo = true;
  }

  public Long getIdFormato() {
    return idFormato;
  }

  public void setIdFormato(Long idFormato) {
    this.idFormato = idFormato;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
    this.biblioteca = biblioteca;
  }

  public String getNombreArchivo() {
    return nombreArchivo;
  }

  public void setNombreArchivo(String nombreArchivo) {
    this.nombreArchivo = nombreArchivo;
  }

  public String getRutaArchivo() {
    return rutaArchivo;
  }

  public void setRutaArchivo(String rutaArchivo) {
    this.rutaArchivo = rutaArchivo;
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

  public Usuario getSubidoPor() {
    return subidoPor;
  }

  public void setSubidoPor(Usuario subidoPor) {
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