package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.time.LocalDate;

import com.proyecto.fhce.library.enums.EstadoPrestamo;

public class PrestamoFiltroDTO {

  private Long bibliotecaId;

  private EstadoPrestamo estado;

  private LocalDate fechaInicio;

  private LocalDate fechaFin;

  public PrestamoFiltroDTO() {
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public EstadoPrestamo getEstado() {
    return estado;
  }

  public void setEstado(EstadoPrestamo estado) {
    this.estado = estado;
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

}
