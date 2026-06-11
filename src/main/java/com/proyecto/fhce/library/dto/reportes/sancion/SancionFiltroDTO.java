package com.proyecto.fhce.library.dto.reportes.sancion;

import java.time.LocalDate;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;

public class SancionFiltroDTO {
  private Long bibliotecaId;
  private Long usuarioId;
  private TipoSancion tipo;
  private EstadoSancion estado;
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

  public TipoSancion getTipo() {
    return tipo;
  }

  public void setTipo(TipoSancion tipo) {
    this.tipo = tipo;
  }

  public EstadoSancion getEstado() {
    return estado;
  }

  public void setEstado(EstadoSancion estado) {
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