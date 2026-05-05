package com.proyecto.fhce.library.dto.request.library;

import java.time.LocalDate;

import com.proyecto.fhce.library.enums.CondicionFisicaLibro;
import com.proyecto.fhce.library.enums.TipoDocumentoGarantia;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

public class ConvertirReservaRequest {

  @NotNull(message = "El ID de la reserva es obligatorio")
  private Long reservaId;

  @NotNull(message = "La condición del libro al entregarlo es obligatoria")
  private CondicionFisicaLibro condicionEntrega;

  @NotNull(message = "El tipo de documento de garantía es obligatorio")
  private TipoDocumentoGarantia tipoDocumentoGarantia;

  @Future(message = "Fecha de devolución debe ser futura")
  private LocalDate fechaDevolucionEstimada;

  private String observaciones;

  public Long getReservaId() {
    return reservaId;
  }

  public void setReservaId(Long reservaId) {
    this.reservaId = reservaId;
  }

  public CondicionFisicaLibro getCondicionEntrega() {
    return condicionEntrega;
  }

  public void setCondicionEntrega(CondicionFisicaLibro condicionEntrega) {
    this.condicionEntrega = condicionEntrega;
  }

  public TipoDocumentoGarantia getTipoDocumentoGarantia() {
    return tipoDocumentoGarantia;
  }

  public void setTipoDocumentoGarantia(TipoDocumentoGarantia tipoDocumentoGarantia) {
    this.tipoDocumentoGarantia = tipoDocumentoGarantia;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public LocalDate getFechaDevolucionEstimada() {
    return fechaDevolucionEstimada;
  }

  public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) {
    this.fechaDevolucionEstimada = fechaDevolucionEstimada;
  }

}
