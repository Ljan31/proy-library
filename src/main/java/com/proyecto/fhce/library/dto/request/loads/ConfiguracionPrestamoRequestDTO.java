package com.proyecto.fhce.library.dto.request.loads;

import java.math.BigDecimal;

import com.proyecto.fhce.library.enums.TipoPrestamo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ConfiguracionPrestamoRequestDTO {

  @NotNull(message = "El tipo de préstamo es obligatorio")
  private TipoPrestamo tipoPrestamo;

  private Long bibliotecaId; // null = configuración global
  private Long rolId; // null = aplica a todos

  @NotNull(message = "Los días de préstamo máximo son obligatorios")
  @Min(value = 1, message = "Debe ser al menos 1 día")
  private Integer diasPrestamoMax;

  @Min(value = 0)
  private Integer ejemplaresMaxDomicilio;

  @Min(value = 0)
  private Integer ejemplaresMaxSala;

  @DecimalMin(value = "0.0")
  private BigDecimal multaPorDia;

  @Min(value = 0)
  private Integer multaMaxDias;

  @Min(value = 0)
  private Integer diasSuspension;

  @Min(value = 1)
  private Integer diasReserva;

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

  public Long getRolId() {
    return rolId;
  }

  public void setRolId(Long rolId) {
    this.rolId = rolId;
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
