package com.proyecto.fhce.library.dto.reportes.sancion;

import java.math.BigDecimal;
import java.time.LocalDate;

public class SancionReporteItemDTO {
  private Long idSancion;
  private String usuario;
  private String ci;
  private String biblioteca;
  private String tipoSancion; // MULTA | SUSPENSION
  private String estadoSancion; // ACTIVA | PAGADA | CONDONADA
  private BigDecimal monto;
  private Integer diasSuspension;
  private Integer diasRetraso;
  private LocalDate fechaInicio;
  private LocalDate fechaFin;
  private Long idPrestamoOrigen;
  private String libroOrigen;

  public Long getIdSancion() {
    return idSancion;
  }

  public void setIdSancion(Long idSancion) {
    this.idSancion = idSancion;
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

  public String getTipoSancion() {
    return tipoSancion;
  }

  public void setTipoSancion(String tipoSancion) {
    this.tipoSancion = tipoSancion;
  }

  public String getEstadoSancion() {
    return estadoSancion;
  }

  public void setEstadoSancion(String estadoSancion) {
    this.estadoSancion = estadoSancion;
  }

  public BigDecimal getMonto() {
    return monto;
  }

  public void setMonto(BigDecimal monto) {
    this.monto = monto;
  }

  public Integer getDiasSuspension() {
    return diasSuspension;
  }

  public void setDiasSuspension(Integer diasSuspension) {
    this.diasSuspension = diasSuspension;
  }

  public Integer getDiasRetraso() {
    return diasRetraso;
  }

  public void setDiasRetraso(Integer diasRetraso) {
    this.diasRetraso = diasRetraso;
  }

  public LocalDate getFechaInicio() {
    return fechaInicio;
  }

  public void setFechaInicio(LocalDate fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  public LocalDate getFechaFin() {
    return fechaFin;
  }

  public void setFechaFin(LocalDate fechaFin) {
    this.fechaFin = fechaFin;
  }

  public Long getIdPrestamoOrigen() {
    return idPrestamoOrigen;
  }

  public void setIdPrestamoOrigen(Long idPrestamoOrigen) {
    this.idPrestamoOrigen = idPrestamoOrigen;
  }

  public String getLibroOrigen() {
    return libroOrigen;
  }

  public void setLibroOrigen(String libroOrigen) {
    this.libroOrigen = libroOrigen;
  }

}