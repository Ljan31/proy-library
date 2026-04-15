package com.proyecto.fhce.library.dto.request.loads;

import java.time.LocalDate;

import com.proyecto.fhce.library.enums.CondicionFisicaLibro;
import com.proyecto.fhce.library.enums.TipoDocumentoGarantia;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PrestamoRequest {
  @NotNull(message = "Ejemplar ID es requerido")
  private Long ejemplarId;

  @NotNull(message = "Usuario ID es requerido")
  private Long usuarioId;

  @NotNull(message = "Biblioteca ID es requerido")
  private Long bibliotecaId;

  @Future(message = "Fecha de devolución debe ser futura")
  private LocalDate fechaDevolucionEstimada;

  private String observaciones;

  @NotNull(message = "El tipo de documento de garantía es obligatorio")
  private TipoDocumentoGarantia tipoDocumentoGarantia;

  @NotNull(message = "La condición física del ejemplar al entregarlo es obligatoria")
  private CondicionFisicaLibro condicionEntrega;

  public Long getEjemplarId() {
    return ejemplarId;
  }

  public void setEjemplarId(Long ejemplarId) {
    this.ejemplarId = ejemplarId;
  }

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

  public LocalDate getFechaDevolucionEstimada() {
    return fechaDevolucionEstimada;
  }

  public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) {
    this.fechaDevolucionEstimada = fechaDevolucionEstimada;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public TipoDocumentoGarantia getTipoDocumentoGarantia() {
    return tipoDocumentoGarantia;
  }

  public void setTipoDocumentoGarantia(TipoDocumentoGarantia tipoDocumentoGarantia) {
    this.tipoDocumentoGarantia = tipoDocumentoGarantia;
  }

  public CondicionFisicaLibro getCondicionEntrega() {
    return condicionEntrega;
  }

  public void setCondicionEntrega(CondicionFisicaLibro condicionEntrega) {
    this.condicionEntrega = condicionEntrega;
  }

}
