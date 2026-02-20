package com.proyecto.fhce.library.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.proyecto.fhce.library.enums.EstadoCertificado;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "no_debt_certificates")
public class CertificadoNoDeuda {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_certificado")
  private Long idCertificado;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Usuario usuario;

  @Column(name = "fecha_emision", nullable = false)
  private LocalDateTime fechaEmision;

  @Column(name = "fecha_vencimiento")
  private LocalDateTime fechaVencimiento;

  @Column(unique = true, nullable = false, length = 100, name = "codigo_verificacion")
  private String codigoVerificacion;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bibliotecario_id", nullable = false)
  private Usuario bibliotecario;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_certificado", nullable = false)
  private EstadoCertificado estadoCertificado;

  @Column(length = 500, name = "pdf_generado")
  private String pdfGenerado;

  @PrePersist
  protected void onCreate() {
    fechaEmision = LocalDateTime.now();
    if (estadoCertificado == null)
      estadoCertificado = EstadoCertificado.VIGENTE;
    if (codigoVerificacion == null) {
      codigoVerificacion = UUID.randomUUID().toString();
    }
  }

  public Long getId_certificado() {
    return idCertificado;
  }

  public void setId_certificado(Long id_certificado) {
    this.idCertificado = id_certificado;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
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
    return codigoVerificacion;
  }

  public void setCodigo_verificacion(String codigo_verificacion) {
    this.codigoVerificacion = codigo_verificacion;
  }

  public Usuario getBibliotecario() {
    return bibliotecario;
  }

  public void setBibliotecario(Usuario bibliotecario) {
    this.bibliotecario = bibliotecario;
  }

  public EstadoCertificado getEstadoCertificado() {
    return estadoCertificado;
  }

  public void setEstadoCertificado(EstadoCertificado estadoCertificado) {
    this.estadoCertificado = estadoCertificado;
  }

  public String getPdf_generado() {
    return pdfGenerado;
  }

  public void setPdf_generado(String pdf_generado) {
    this.pdfGenerado = pdf_generado;
  }

}
