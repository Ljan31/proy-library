package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.time.LocalDate;

import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.TipoPrestamo;

public class HistorialPrestamoFiltroDTO {
  private Long bibliotecaId;

  private Long usuarioId;

  private Long libroId;

  private EstadoPrestamo estado;

  private TipoPrestamo tipoPrestamo;

  private LocalDate fechaInicio;

  private LocalDate fechaFin;

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Long getLibroId() {
    return libroId;
  }

  public void setLibroId(Long libroId) {
    this.libroId = libroId;
  }

  public EstadoPrestamo getEstado() {
    return estado;
  }

  public void setEstado(EstadoPrestamo estado) {
    this.estado = estado;
  }

  public TipoPrestamo getTipoPrestamo() {
    return tipoPrestamo;
  }

  public void setTipoPrestamo(TipoPrestamo tipoPrestamo) {
    this.tipoPrestamo = tipoPrestamo;
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
