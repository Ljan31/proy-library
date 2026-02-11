package com.proyecto.fhce.library.dto.response.loads;

import java.time.LocalDateTime;

import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.enums.EstadoEjemplar;

public class HistorialEstadoResponse {
  private Long id_historial;
  private EstadoEjemplar estadoAnterior;
  private EstadoEjemplar estadoNuevo;
  private LocalDateTime fechaCambio;
  private UsuarioSimpleResponse usuarioCambio;
  private String motivo;

  public Long getId_historial() {
    return id_historial;
  }

  public void setId_historial(Long id_historial) {
    this.id_historial = id_historial;
  }

  public EstadoEjemplar getEstadoAnterior() {
    return estadoAnterior;
  }

  public void setEstadoAnterior(EstadoEjemplar estadoAnterior) {
    this.estadoAnterior = estadoAnterior;
  }

  public EstadoEjemplar getEstadoNuevo() {
    return estadoNuevo;
  }

  public void setEstadoNuevo(EstadoEjemplar estadoNuevo) {
    this.estadoNuevo = estadoNuevo;
  }

  public LocalDateTime getFechaCambio() {
    return fechaCambio;
  }

  public void setFechaCambio(LocalDateTime fechaCambio) {
    this.fechaCambio = fechaCambio;
  }

  public UsuarioSimpleResponse getUsuarioCambio() {
    return usuarioCambio;
  }

  public void setUsuarioCambio(UsuarioSimpleResponse usuarioCambio) {
    this.usuarioCambio = usuarioCambio;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }

}
