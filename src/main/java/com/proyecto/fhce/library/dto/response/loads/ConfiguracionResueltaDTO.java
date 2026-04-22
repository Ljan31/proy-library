package com.proyecto.fhce.library.dto.response.loads;

import java.math.BigDecimal;

import com.proyecto.fhce.library.enums.TipoPrestamo;

public class ConfiguracionResueltaDTO {

  private Long idConfig;
  private TipoPrestamo tipoPrestamo;
  private Integer diasPrestamoMax;
  private Integer ejemplaresPermitidos;
  private BigDecimal multaPorDia;
  private Integer multaMaxDias;
  private Integer diasSuspension;
  private Integer diasReserva;
  private String nivelAplicacion; // "ROL_BIBLIOTECA" | "BIBLIOTECA" | "GLOBAL"

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

  public Integer getDiasPrestamoMax() {
    return diasPrestamoMax;
  }

  public void setDiasPrestamoMax(Integer diasPrestamoMax) {
    this.diasPrestamoMax = diasPrestamoMax;
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

  public String getNivelAplicacion() {
    return nivelAplicacion;
  }

  public void setNivelAplicacion(String nivelAplicacion) {
    this.nivelAplicacion = nivelAplicacion;
  }

}