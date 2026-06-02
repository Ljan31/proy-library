package com.proyecto.fhce.library.dto.reportes;

public class DashboardDTO {
  private String nombreBiblioteca;
  private Long totalLibros;
  private Long totalEjemplares;
  private Long totalPrestamosActivos;
  private Long totalPrestamosVencidos;
  private Long totalUsuarios;
  private Long totalReservas;
  private Long totalSancionesActivas;
  private Long totalCertificadosEmitidos;

  public DashboardDTO() {
  }

  public Long getTotalLibros() {
    return totalLibros;
  }

  public void setTotalLibros(Long totalLibros) {
    this.totalLibros = totalLibros;
  }

  public Long getTotalEjemplares() {
    return totalEjemplares;
  }

  public void setTotalEjemplares(Long totalEjemplares) {
    this.totalEjemplares = totalEjemplares;
  }

  public Long getTotalPrestamosActivos() {
    return totalPrestamosActivos;
  }

  public void setTotalPrestamosActivos(Long totalPrestamosActivos) {
    this.totalPrestamosActivos = totalPrestamosActivos;
  }

  public Long getTotalPrestamosVencidos() {
    return totalPrestamosVencidos;
  }

  public void setTotalPrestamosVencidos(Long totalPrestamosVencidos) {
    this.totalPrestamosVencidos = totalPrestamosVencidos;
  }

  public Long getTotalUsuarios() {
    return totalUsuarios;
  }

  public void setTotalUsuarios(Long totalUsuarios) {
    this.totalUsuarios = totalUsuarios;
  }

  public Long getTotalReservas() {
    return totalReservas;
  }

  public void setTotalReservas(Long totalReservas) {
    this.totalReservas = totalReservas;
  }

  public Long getTotalSancionesActivas() {
    return totalSancionesActivas;
  }

  public void setTotalSancionesActivas(Long totalSancionesActivas) {
    this.totalSancionesActivas = totalSancionesActivas;
  }

  public Long getTotalCertificadosEmitidos() {
    return totalCertificadosEmitidos;
  }

  public void setTotalCertificadosEmitidos(Long totalCertificadosEmitidos) {
    this.totalCertificadosEmitidos = totalCertificadosEmitidos;
  }

  public String getNombreBiblioteca() {
    return nombreBiblioteca;
  }

  public void setNombreBiblioteca(String nombreBiblioteca) {
    this.nombreBiblioteca = nombreBiblioteca;
  }
}