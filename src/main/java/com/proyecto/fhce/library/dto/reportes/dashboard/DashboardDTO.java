package com.proyecto.fhce.library.dto.reportes.dashboard;

import java.util.List;

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

  // Gráfica 1
  private List<PeriodoPrestamosDTO> prestamosPorMes;

  // Gráfica 2
  private List<PrestamoEstadoDTO> prestamosPorEstado;

  // Gráfica 3
  private List<LibroMasPrestadoDashboardDTO> librosMasPrestados;

  // Gráfica 4
  private List<PrestamoPorTipoDTO> prestamosPorTipo;

  // Gráfica 5
  private List<PrestamoPorBibliotecaDTO> prestamosPorBiblioteca;

  // Gráfica 6
  private List<DevolucionPorMesDTO> devolucionesPorMes;

  // Gráfica 7
  private List<CondicionDevolucionDTO> condicionDevoluciones;

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

  public List<PeriodoPrestamosDTO> getPrestamosPorMes() {
    return prestamosPorMes;
  }

  public void setPrestamosPorMes(List<PeriodoPrestamosDTO> prestamosPorMes) {
    this.prestamosPorMes = prestamosPorMes;
  }

  public List<PrestamoEstadoDTO> getPrestamosPorEstado() {
    return prestamosPorEstado;
  }

  public void setPrestamosPorEstado(List<PrestamoEstadoDTO> prestamosPorEstado) {
    this.prestamosPorEstado = prestamosPorEstado;
  }

  public List<LibroMasPrestadoDashboardDTO> getLibrosMasPrestados() {
    return librosMasPrestados;
  }

  public void setLibrosMasPrestados(List<LibroMasPrestadoDashboardDTO> librosMasPrestados) {
    this.librosMasPrestados = librosMasPrestados;
  }

  public List<PrestamoPorTipoDTO> getPrestamosPorTipo() {
    return prestamosPorTipo;
  }

  public void setPrestamosPorTipo(List<PrestamoPorTipoDTO> prestamosPorTipo) {
    this.prestamosPorTipo = prestamosPorTipo;
  }

  public List<PrestamoPorBibliotecaDTO> getPrestamosPorBiblioteca() {
    return prestamosPorBiblioteca;
  }

  public void setPrestamosPorBiblioteca(List<PrestamoPorBibliotecaDTO> prestamosPorBiblioteca) {
    this.prestamosPorBiblioteca = prestamosPorBiblioteca;
  }

  public List<DevolucionPorMesDTO> getDevolucionesPorMes() {
    return devolucionesPorMes;
  }

  public void setDevolucionesPorMes(List<DevolucionPorMesDTO> devolucionesPorMes) {
    this.devolucionesPorMes = devolucionesPorMes;
  }

  public List<CondicionDevolucionDTO> getCondicionDevoluciones() {
    return condicionDevoluciones;
  }

  public void setCondicionDevoluciones(List<CondicionDevolucionDTO> condicionDevoluciones) {
    this.condicionDevoluciones = condicionDevoluciones;
  }
}