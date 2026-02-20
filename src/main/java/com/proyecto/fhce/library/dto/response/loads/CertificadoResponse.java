package com.proyecto.fhce.library.dto.response.loads;

import java.time.LocalDateTime;

import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.enums.EstadoCertificado;

public class CertificadoResponse {
  private Long id_certificado;
  private UsuarioSimpleResponse usuario;
  private LocalDateTime fechaEmision;
  private LocalDateTime fechaVencimiento;
  private String codigo_verificacion;
  private UsuarioSimpleResponse bibliotecario;
  private EstadoCertificado estadoCertificado;
  private String pdf_generado;
  private String urlDescarga;

  public Long getId_certificado() {
    return id_certificado;
  }

  public void setId_certificado(Long id_certificado) {
    this.id_certificado = id_certificado;
  }

  public UsuarioSimpleResponse getUsuario() {
    return usuario;
  }

  public void setUsuario(UsuarioSimpleResponse usuario) {
    this.usuario = usuario;
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

  public String getCodigo_verificacion() {
    return codigo_verificacion;
  }

  public void setCodigo_verificacion(String codigo_verificacion) {
    this.codigo_verificacion = codigo_verificacion;
  }

  public UsuarioSimpleResponse getBibliotecario() {
    return bibliotecario;
  }

  public void setBibliotecario(UsuarioSimpleResponse bibliotecario) {
    this.bibliotecario = bibliotecario;
  }

  public EstadoCertificado getEstadoCertificado() {
    return estadoCertificado;
  }

  public void setEstadoCertificado(EstadoCertificado estadoCertificado) {
    this.estadoCertificado = estadoCertificado;
  }

  public String getPdf_generado() {
    return pdf_generado;
  }

  public void setPdf_generado(String pdf_generado) {
    this.pdf_generado = pdf_generado;
  }

  public String getUrlDescarga() {
    return urlDescarga;
  }

  public void setUrlDescarga(String urlDescarga) {
    this.urlDescarga = urlDescarga;
  }

}