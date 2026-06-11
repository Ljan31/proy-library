package com.proyecto.fhce.library.dto.reportes.certificado;

import java.time.LocalDateTime;

public class CertificadoReporteItemDTO {
  private Long idCertificado;
  private String codigoVerificacion;
  private String usuario;
  private String ci;
  private String biblioteca;
  private String bibliotecario;
  private LocalDateTime fechaEmision;
  private LocalDateTime fechaVencimiento;
  private String estadoCertificado; // VIGENTE | VENCIDO | ANULADO

  public Long getIdCertificado() {
    return idCertificado;
  }

  public void setIdCertificado(Long idCertificado) {
    this.idCertificado = idCertificado;
  }

  public String getCodigoVerificacion() {
    return codigoVerificacion;
  }

  public void setCodigoVerificacion(String codigoVerificacion) {
    this.codigoVerificacion = codigoVerificacion;
  }

  public String getUsuario() {
    return usuario;
  }

  public void setUsuario(String usuario) {
    this.usuario = usuario;
  }

  public String getCi() {
    return ci;
  }

  public void setCi(String ci) {
    this.ci = ci;
  }

  public String getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(String biblioteca) {
    this.biblioteca = biblioteca;
  }

  public String getBibliotecario() {
    return bibliotecario;
  }

  public void setBibliotecario(String bibliotecario) {
    this.bibliotecario = bibliotecario;
  }

  public LocalDateTime getFechaEmision() {
    return fechaEmision;
  }

  public void setFechaEmision(LocalDateTime fechaEmision) {
    this.fechaEmision = fechaEmision;
  }

  public LocalDateTime getFechaVencimiento() {
    return fechaVencimiento;
  }

  public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
    this.fechaVencimiento = fechaVencimiento;
  }

  public String getEstadoCertificado() {
    return estadoCertificado;
  }

  public void setEstadoCertificado(String estadoCertificado) {
    this.estadoCertificado = estadoCertificado;
  }

}