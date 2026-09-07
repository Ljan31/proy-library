package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PrestamoActivoDTO {

  // Identificación del préstamo
  private Long idPrestamo;
  private String estadoPrestamo; // ACTIVO | RENOVADO | VENCIDO
  private String tipoPrestamo; // SALA | DOMICILIO

  // Fechas
  private LocalDateTime fechaPrestamo;
  private LocalDate fechaDevolucionEstimada;

  // Campo calculado en Java — no hardcodeado en la query
  private boolean vencido;
  private int diasRetraso; // > 0 solo en vencidos; en por-vencer es negativo (días que faltan)
  private int diasRestantes; // días hasta vencer; negativo si ya venció

  // Usuario (lector)
  private Long idUsuario;
  private String nombreUsuario;
  private String ci;

  // Libro y ejemplar
  private Long idEjemplar;
  private String codigoEjemplar;
  private String tituloLibro;
  private String isbn;
  private String editorial;

  // Biblioteca
  private Long idBiblioteca;
  private String nombreBiblioteca;

  // Renovaciones
  private Integer renovaciones;

  public Long getIdPrestamo() {
    return idPrestamo;
  }

  public void setIdPrestamo(Long idPrestamo) {
    this.idPrestamo = idPrestamo;
  }

  public String getEstadoPrestamo() {
    return estadoPrestamo;
  }

  public void setEstadoPrestamo(String estadoPrestamo) {
    this.estadoPrestamo = estadoPrestamo;
  }

  public String getTipoPrestamo() {
    return tipoPrestamo;
  }

  public void setTipoPrestamo(String tipoPrestamo) {
    this.tipoPrestamo = tipoPrestamo;
  }

  public LocalDateTime getFechaPrestamo() {
    return fechaPrestamo;
  }

  public void setFechaPrestamo(LocalDateTime fechaPrestamo) {
    this.fechaPrestamo = fechaPrestamo;
  }

  public LocalDate getFechaDevolucionEstimada() {
    return fechaDevolucionEstimada;
  }

  public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) {
    this.fechaDevolucionEstimada = fechaDevolucionEstimada;
  }

  public boolean isVencido() {
    return vencido;
  }

  public void setVencido(boolean vencido) {
    this.vencido = vencido;
  }

  public int getDiasRetraso() {
    return diasRetraso;
  }

  public void setDiasRetraso(int diasRetraso) {
    this.diasRetraso = diasRetraso;
  }

  public int getDiasRestantes() {
    return diasRestantes;
  }

  public void setDiasRestantes(int diasRestantes) {
    this.diasRestantes = diasRestantes;
  }

  public Long getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(Long idUsuario) {
    this.idUsuario = idUsuario;
  }

  public String getNombreUsuario() {
    return nombreUsuario;
  }

  public void setNombreUsuario(String nombreUsuario) {
    this.nombreUsuario = nombreUsuario;
  }

  public String getCi() {
    return ci;
  }

  public void setCi(String ci) {
    this.ci = ci;
  }

  public Long getIdEjemplar() {
    return idEjemplar;
  }

  public void setIdEjemplar(Long idEjemplar) {
    this.idEjemplar = idEjemplar;
  }

  public String getCodigoEjemplar() {
    return codigoEjemplar;
  }

  public void setCodigoEjemplar(String codigoEjemplar) {
    this.codigoEjemplar = codigoEjemplar;
  }

  public String getTituloLibro() {
    return tituloLibro;
  }

  public void setTituloLibro(String tituloLibro) {
    this.tituloLibro = tituloLibro;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getEditorial() {
    return editorial;
  }

  public void setEditorial(String editorial) {
    this.editorial = editorial;
  }

  public Long getIdBiblioteca() {
    return idBiblioteca;
  }

  public void setIdBiblioteca(Long idBiblioteca) {
    this.idBiblioteca = idBiblioteca;
  }

  public String getNombreBiblioteca() {
    return nombreBiblioteca;
  }

  public void setNombreBiblioteca(String nombreBiblioteca) {
    this.nombreBiblioteca = nombreBiblioteca;
  }

  public Integer getRenovaciones() {
    return renovaciones;
  }

  public void setRenovaciones(Integer renovaciones) {
    this.renovaciones = renovaciones;
  }

}
