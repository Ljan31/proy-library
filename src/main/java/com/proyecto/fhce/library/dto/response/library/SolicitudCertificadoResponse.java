package com.proyecto.fhce.library.dto.response.library;

import java.time.LocalDateTime;

public class SolicitudCertificadoResponse {

  private Long id;

  private String nombres;

  private String apellidos;

  private String ci;

  private String matricula;

  private String bibliotecaNombre;

  private String razon;

  private String descripcion;

  private String estado;

  private LocalDateTime fechaSolicitud;

  private LocalDateTime fechaRespuesta;

  private String observacionRespuesta;
  private String email;

  private String telefono;

  public SolicitudCertificadoResponse() {
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

}