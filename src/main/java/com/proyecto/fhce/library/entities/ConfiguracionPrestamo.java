package com.proyecto.fhce.library.entities;

import java.math.BigDecimal;

import com.proyecto.fhce.library.enums.TipoPrestamo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "configuracion_prestamo", uniqueConstraints = {
    @UniqueConstraint(name = "uk_config_biblioteca_rol_tipo", columnNames = { "biblioteca_id", "rol_id",
        "tipo_prestamo" })
}, indexes = {
    @Index(name = "idx_config_biblioteca", columnList = "biblioteca_id"),
    @Index(name = "idx_config_rol", columnList = "rol_id")
})
public class ConfiguracionPrestamo {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_config")
  private Long idConfig;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "biblioteca_id")
  private Biblioteca biblioteca;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "rol_id")
  private Role rol;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_prestamo", nullable = false, length = 20)
  private TipoPrestamo tipoPrestamo;

  @Column(name = "dias_prestamo_max", nullable = false)
  @Min(value = 1, message = "Los días de préstamo máximo deben ser al menos 1")
  private Integer diasPrestamoMax;

  @Column(name = "ejemplares_max_domicilio")
  @Min(value = 0, message = "El límite de ejemplares no puede ser negativo")
  private Integer ejemplaresMaxDomicilio;

  @Column(name = "ejemplares_max_sala")
  @Min(value = 0, message = "El límite de ejemplares en sala no puede ser negativo")
  private Integer ejemplaresMaxSala;

  @Column(name = "multa_por_dia", precision = 10, scale = 2)
  @DecimalMin(value = "0.0", message = "La multa por día no puede ser negativa")
  private BigDecimal multaPorDia;

  @Column(name = "multa_max_dias")
  @Min(value = 0, message = "El máximo de días de multa no puede ser negativo")
  private Integer multaMaxDias;

  @Column(name = "dias_suspension")
  @Min(value = 0, message = "Los días de suspensión no pueden ser negativos")
  private Integer diasSuspension;

  @Column(name = "dias_reserva")
  @Min(value = 1, message = "Los días de reserva deben ser al menos 1")
  private Integer diasReserva;

  public Long getIdConfig() {
    return idConfig;
  }

  public void setIdConfig(Long idConfig) {
    this.idConfig = idConfig;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Role getRol() {
    return rol;
  }

  public void setRol(Role rol) {
    this.rol = rol;
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