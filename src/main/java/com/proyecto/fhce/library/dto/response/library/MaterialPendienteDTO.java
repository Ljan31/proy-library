package com.proyecto.fhce.library.dto.response.library;

import java.time.LocalDate;

public class MaterialPendienteDTO {
  private String tituloLibro;
  private String codigoEjemplar;
  private LocalDate fechaDevolucionEstimada;
  private String estadoPrestamo;

  public String getTituloLibro() {
    return tituloLibro;
  }

  public void setTituloLibro(String tituloLibro) {
    this.tituloLibro = tituloLibro;
  }

  public String getCodigoEjemplar() {
    return codigoEjemplar;
  }

  public void setCodigoEjemplar(String codigoEjemplar) {
    this.codigoEjemplar = codigoEjemplar;
  }

  public LocalDate getFechaDevolucionEstimada() {
    return fechaDevolucionEstimada;
  }

  public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) {
    this.fechaDevolucionEstimada = fechaDevolucionEstimada;
  }

  public String getEstadoPrestamo() {
    return estadoPrestamo;
  }

  public void setEstadoPrestamo(String estadoPrestamo) {
    this.estadoPrestamo = estadoPrestamo;
  }

}
