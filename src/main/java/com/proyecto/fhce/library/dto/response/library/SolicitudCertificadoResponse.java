package com.proyecto.fhce.library.dto.response.library;

import java.time.LocalDateTime;

public class SolicitudCertificadoResponse {

  private Long id;

  private String nombres;
  private Long usuarioId;
  private String apellidos;

  private String ci;

  private String matricula;

  private Long bibliotecaId;
  private String bibliotecaNombre;

  private Long razonId; // ← Nuevo
  private String razonNombre; // ← Recomendado (para mostrar en frontend)
  private String requisitos;

  private String descripcion;

  private String estado;

  private LocalDateTime fechaSolicitud;

  private LocalDateTime fechaRespuesta;

  private String observacionRespuesta;
  private String email;

  private String telefono;
  private Long atendidoPorId;
  private String atendidoPorNombre;

  public SolicitudCertificadoResponse() {
  }

  // Constructor completo
  public SolicitudCertificadoResponse(Long id, Long usuarioId, String nombres, String apellidos,
      String ci, String matricula, String email, String telefono,
      Long bibliotecaId, String bibliotecaNombre,
      Long razonId, String razonNombre, String requisitos,
      String descripcion, String estado, LocalDateTime fechaSolicitud,
      LocalDateTime fechaRespuesta, String observacionRespuesta) {

    this.id = id;
    this.usuarioId = usuarioId;
    this.nombres = nombres;
    this.apellidos = apellidos;
    this.ci = ci;
    this.matricula = matricula;
    this.email = email;
    this.telefono = telefono;
    this.bibliotecaId = bibliotecaId;
    this.bibliotecaNombre = bibliotecaNombre;
    this.razonId = razonId;
    this.razonNombre = razonNombre;
    this.requisitos = requisitos;
    this.descripcion = descripcion;
    this.estado = estado;
    this.fechaSolicitud = fechaSolicitud;
    this.fechaRespuesta = fechaRespuesta;
    this.observacionRespuesta = observacionRespuesta;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getBibliotecaNombre() {
    return bibliotecaNombre;
  }

  public void setBibliotecaNombre(String bibliotecaNombre) {
    this.bibliotecaNombre = bibliotecaNombre;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  public String getEstado() {
    return estado;
  }

  public void setEstado(String estado) {
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

  public Long getRazonId() {
    return razonId;
  }

  public void setRazonId(Long razonId) {
    this.razonId = razonId;
  }

  public String getRazonNombre() {
    return razonNombre;
  }

  public void setRazonNombre(String razonNombre) {
    this.razonNombre = razonNombre;
  }

  public String getRequisitos() {
    return requisitos;
  }

  public void setRequisitos(String requisitos) {
    this.requisitos = requisitos;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public Long getAtendidoPorId() {
    return atendidoPorId;
  }

  public void setAtendidoPorId(Long atendidoPorId) {
    this.atendidoPorId = atendidoPorId;
  }

  public String getAtendidoPorNombre() {
    return atendidoPorNombre;
  }

  public void setAtendidoPorNombre(String atendidoPorNombre) {
    this.atendidoPorNombre = atendidoPorNombre;
  }

}