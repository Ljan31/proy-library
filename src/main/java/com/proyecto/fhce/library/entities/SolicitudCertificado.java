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

  @Column(length = 100)
  private String email;

  @Column(length = 20)
  private String telefono;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "razon_certificado_id", nullable = false)
  private RazonCertificado razonCertificado;

  @Column(length = 500)
  private String descripcion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EstadoSolicitud estado;

  @Column(nullable = false)
  private LocalDateTime fechaSolicitud;

  private LocalDateTime fechaRespuesta;
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "atendido_por_id", nullable = true)
  private Usuario atendidoPor;

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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public RazonCertificado getRazonCertificado() {
    return razonCertificado;
  }

  public void setRazonCertificado(RazonCertificado razonCertificado) {
    this.razonCertificado = razonCertificado;
  }

  public Usuario getAtendidoPor() {
    return atendidoPor;
  }

  public void setAtendidoPor(Usuario atendidoPor) {
    this.atendidoPor = atendidoPor;
  }

}
