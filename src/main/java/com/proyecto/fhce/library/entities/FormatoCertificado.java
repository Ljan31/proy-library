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

  @Column(nullable = false)
  private Integer version;

  @Column(nullable = false)
  private Boolean activo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "subido_por")
  private Usuario subidoPor;

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
}