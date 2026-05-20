package com.proyecto.fhce.library.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "certificate_reasons")
public class RazonCertificado {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_razon")
  private Long idRazon;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "biblioteca_id", nullable = true)
  private Biblioteca biblioteca;

  @Column(nullable = false, length = 150)
  private String nombre;

  @Column(length = 500)
  private String descripcion;

  @Column(name = "requisitos", columnDefinition = "TEXT")
  private String requisitos;

  @Column(nullable = false)
  private Boolean activo = true;

  public Long getIdRazon() {
    return idRazon;
  }

  public void setIdRazon(Long idRazon) {
    this.idRazon = idRazon;
  }

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
    this.biblioteca = biblioteca;
  }

  public String getRequisitos() {
    return requisitos;
  }

  public void setRequisitos(String requisitos) {
    this.requisitos = requisitos;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

}
