package com.proyecto.fhce.library.dto.response.loads;

import java.math.BigDecimal;

import com.proyecto.fhce.library.enums.TipoPrestamo;

public class ConfiguracionPrestamoResponseDTO {

  private Long idConfig;
  private TipoPrestamo tipoPrestamo;

  // Datos resumidos de relaciones — nunca la entidad completa
  private Long bibliotecaId;
  private String nombreBiblioteca;
  private Long rolId;
  private String nombreRol;

  private Integer diasPrestamoMax;
  private Integer ejemplaresMaxDomicilio;
  private Integer ejemplaresMaxSala;
  private BigDecimal multaPorDia;
  private Integer multaMaxDias;
  private Integer diasSuspension;
  private Integer diasReserva;

  public Long getIdConfig() {
    return idConfig;
  }

  public void setIdConfig(Long idConfig) {
    this.idConfig = idConfig;
  }

  public TipoPrestamo getTipoPrestamo() {
    return tipoPrestamo;
  }

  public void setTipoPrestamo(TipoPrestamo tipoPrestamo) {
    this.tipoPrestamo = tipoPrestamo;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getNombreBiblioteca() {
    return nombreBiblioteca;
  }

  public void setNombreBiblioteca(String nombreBiblioteca) {
    this.nombreBiblioteca = nombreBiblioteca;
  }

  public Long getRolId() {
    return rolId;
  }

  public void setRolId(Long rolId) {
    this.rolId = rolId;
  }

  public String getNombreRol() {
    return nombreRol;
  }

  public void setNombreRol(String nombreRol) {
    this.nombreRol = nombreRol;
  }

  public Integer getDiasPrestamoMax() {
    return diasPrestamoMax;
  }

  public void setDiasPrestamoMax(Integer diasPrestamoMax) {
    this.diasPrestamoMax = diasPrestamoMax;
  }

  public Integer getEjemplaresMaxDomicilio() {
    return ejemplaresMaxDomicilio;
  }

  public void setEjemplaresMaxDomicilio(Integer ejemplaresMaxDomicilio) {
    this.ejemplaresMaxDomicilio = ejemplaresMaxDomicilio;
  }

  public Integer getEjemplaresMaxSala() {
    return ejemplaresMaxSala;
  }

  public void setEjemplaresMaxSala(Integer ejemplaresMaxSala) {
    this.ejemplaresMaxSala = ejemplaresMaxSala;
  }

  public BigDecimal getMultaPorDia() {
    return multaPorDia;
  }

  public void setMultaPorDia(BigDecimal multaPorDia) {
    this.multaPorDia = multaPorDia;
  }

  public Integer getMultaMaxDias() {
    return multaMaxDias;
  }

  public void setMultaMaxDias(Integer multaMaxDias) {
    this.multaMaxDias = multaMaxDias;
  }

  public Integer getDiasSuspension() {
    return diasSuspension;
  }

  public void setDiasSuspension(Integer diasSuspension) {
    this.diasSuspension = diasSuspension;
  }

  public Integer getDiasReserva() {
    return diasReserva;
  }

  public void setDiasReserva(Integer diasReserva) {
    this.diasReserva = diasReserva;
  }

}