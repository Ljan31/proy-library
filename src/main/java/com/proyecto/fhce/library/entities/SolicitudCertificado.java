package com.proyecto.fhce.library.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import com.proyecto.fhce.library.enums.EstadoSolicitud;

@Entity
@Table(name = "solicitudes_certificado")
public class SolicitudCertificado {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Relación opcional con usuario
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = true)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "biblioteca_id", nullable = false)
  private Biblioteca biblioteca;

  @Column(nullable = false)
  private String nombres;

  @Column(nullable = false)
  private String apellidos;

  @Column(nullable = false, length = 20)
  private String ci;

  @Column(length = 50)
  private String matricula;

  @Column(nullable = false, length = 200)
  private String razon;

  @Column(length = 500)
  private String descripcion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoSolicitud estado;

  @Column(nullable = false)
  private LocalDateTime fechaSolicitud;

  private LocalDateTime fechaRespuesta;

  @Column(length = 500)
  private String observacionRespuesta;

  public SolicitudCertificado() {
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
    this.biblioteca = biblioteca;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApellidos() {
    return apellidos;
  }

  public void setApellidos(String apellidos) {
    this.apellidos = apellidos;
  }

  public String getCi() {
    return ci;
  }

  public void setCi(String ci) {
    this.ci = ci;
  }

  public String getMatricula() {
    return matricula;
  }

  public void setMatricula(String matricula) {
    this.matricula = matricula;
  }

  public String getRazon() {
    return razon;
  }

  public void setRazon(String razon) {
    this.razon = razon;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public EstadoSolicitud getEstado() {
    return estado;
  }

  public void setEstado(EstadoSolicitud estado) {
    this.estado = estado;
  }

  public LocalDateTime getFechaSolicitud() {
    return fechaSolicitud;
  }

  public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
    this.fechaSolicitud = fechaSolicitud;
  }

  public LocalDateTime getFechaRespuesta() {
    return fechaRespuesta;
  }

  public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
    this.fechaRespuesta = fechaRespuesta;
  }

  public String getObservacionRespuesta() {
    return observacionRespuesta;
  }

  public void setObservacionRespuesta(String observacionRespuesta) {
    this.observacionRespuesta = observacionRespuesta;
  }

}
