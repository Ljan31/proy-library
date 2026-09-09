package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HistorialPrestamoDTO {
  private Long idPrestamo;

  private String usuario;

  private String ci;

  private String libro;

  private String biblioteca;

  private String tipoPrestamo;

  private String estadoPrestamo;

  private LocalDateTime fechaPrestamo;

  private LocalDate fechaDevolucionEstimada;

  private LocalDateTime fechaDevolucionReal;

  private Integer renovaciones;

  private String condicionEntrega;

  private String condicionDevolucion;
  private boolean entregadoConDeterioro;

  private Integer diasRetraso;

  public Long getIdPrestamo() {
    return idPrestamo;
  }

  public void setIdPrestamo(Long idPrestamo) {
    this.idPrestamo = idPrestamo;
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

  public String getLibro() {
    return libro;
  }

  public void setLibro(String libro) {
    this.libro = libro;
  }

  public String getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(String biblioteca) {
    this.biblioteca = biblioteca;
  }

  public String getTipoPrestamo() {
    return tipoPrestamo;
  }

  public void setTipoPrestamo(String tipoPrestamo) {
    this.tipoPrestamo = tipoPrestamo;
  }

  public String getEstadoPrestamo() {
    return estadoPrestamo;
  }

  public void setEstadoPrestamo(String estadoPrestamo) {
    this.estadoPrestamo = estadoPrestamo;
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

  public LocalDateTime getFechaDevolucionReal() {
    return fechaDevolucionReal;
  }

  public void setFechaDevolucionReal(LocalDateTime fechaDevolucionReal) {
    this.fechaDevolucionReal = fechaDevolucionReal;
  }

  public Integer getRenovaciones() {
    return renovaciones;
  }

  public void setRenovaciones(Integer renovaciones) {
    this.renovaciones = renovaciones;
  }

  public String getCondicionEntrega() {
    return condicionEntrega;
  }

  public void setCondicionEntrega(String condicionEntrega) {
    this.condicionEntrega = condicionEntrega;
  }

  public String getCondicionDevolucion() {
    return condicionDevolucion;
  }

  public void setCondicionDevolucion(String condicionDevolucion) {
    this.condicionDevolucion = condicionDevolucion;
  }

  public Integer getDiasRetraso() {
    return diasRetraso;
  }

  public void setDiasRetraso(Integer diasRetraso) {
    this.diasRetraso = diasRetraso;
  }

  public boolean isEntregadoConDeterioro() {
    return entregadoConDeterioro;
  }

  public void setEntregadoConDeterioro(boolean entregadoConDeterioro) {
    this.entregadoConDeterioro = entregadoConDeterioro;
  }

}
