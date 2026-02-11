package com.proyecto.fhce.library.dto.request.loads;

import java.time.LocalDate;

import com.proyecto.fhce.library.enums.EstadoPrestamo;

public class FiltroPrestamoRequest {
  private Long usuarioId;
  private Long bibliotecaId;
  private EstadoPrestamo estadoPrestamo;
  private LocalDate fechaDesde;
  private LocalDate fechaHasta;
  private Boolean vencidos;
  private Boolean porVencer; // próximos 3 días

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public EstadoPrestamo getEstadoPrestamo() {
    return estadoPrestamo;
  }

  public void setEstadoPrestamo(EstadoPrestamo estadoPrestamo) {
    this.estadoPrestamo = estadoPrestamo;
  }

  public LocalDate getFechaDesde() {
    return fechaDesde;
  }

  public void setFechaDesde(LocalDate fechaDesde) {
    this.fechaDesde = fechaDesde;
  }

  public LocalDate getFechaHasta() {
    return fechaHasta;
  }

  public void setFechaHasta(LocalDate fechaHasta) {
    this.fechaHasta = fechaHasta;
  }

  public Boolean getVencidos() {
    return vencidos;
  }

  public void setVencidos(Boolean vencidos) {
    this.vencidos = vencidos;
  }

  public Boolean getPorVencer() {
    return porVencer;
  }

  public void setPorVencer(Boolean porVencer) {
    this.porVencer = porVencer;
  }

}
