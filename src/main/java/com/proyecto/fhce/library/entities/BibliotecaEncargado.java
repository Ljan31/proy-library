package com.proyecto.fhce.library.entities;

import java.time.LocalDateTime;

import com.proyecto.fhce.library.enums.RolEncargado;

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
import jakarta.persistence.Table;

@Entity
@Table(name = "biblioteca_encargados")
public class BibliotecaEncargado {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "biblioteca_id", nullable = false)
  private Biblioteca biblioteca;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  @Enumerated(EnumType.STRING)
  @Column(name = "rol_encargado", nullable = false)
  private RolEncargado rolEncargado; // PRINCIPAL, AUXILIAR

  @Column(name = "fecha_asignacion", nullable = false)
  private LocalDateTime fechaAsignacion;

  @Column(name = "fecha_fin")
  private LocalDateTime fechaFin; // null = activo

  @Column(nullable = false)
  private Boolean activo = true;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public RolEncargado getRolEncargado() {
    return rolEncargado;
  }

  public void setRolEncargado(RolEncargado rolEncargado) {
    this.rolEncargado = rolEncargado;
  }

  public LocalDateTime getFechaAsignacion() {
    return fechaAsignacion;
  }

  public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
    this.fechaAsignacion = fechaAsignacion;
  }

  public LocalDateTime getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDateTime fechaFin) {
    this.fechaFin = fechaFin;
  }

  public Boolean getActivo() {
    return activo;
  }

  public void setActivo(Boolean activo) {
    this.activo = activo;
  }

}