package com.proyecto.fhce.library.dto;

import java.math.BigDecimal;

public class ReglasPrestamoDTO {

  private Long idConfig;

  // Solo los campos relevantes según el tipo de préstamo consultado
  private Integer diasPrestamoMax;
  private Integer renovacionesMax;
  private Integer ejemplaresPermitidos;
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

  public Integer getDiasPrestamoMax() {
    return diasPrestamoMax;
  }

  public void setDiasPrestamoMax(Integer diasPrestamoMax) {
    this.diasPrestamoMax = diasPrestamoMax;
  }

  public Integer getRenovacionesMax() {
    return renovacionesMax;
  }

  public void setRenovacionesMax(Integer renovacionesMax) {
    this.renovacionesMax = renovacionesMax;
  }

  public Integer getEjemplaresPermitidos() {
    return ejemplaresPermitidos;
  }

  public void setEjemplaresPermitidos(Integer ejemplaresPermitidos) {
    this.ejemplaresPermitidos = ejemplaresPermitidos;
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