package com.proyecto.fhce.library.dto.response.loads;

import java.time.LocalDate;

import com.proyecto.fhce.library.enums.EstadoPrestamo;

public class PrestamoSimpleResponse {
  private Long id_prestamo;
  private String tituloLibro;
  private String codigoEjemplar;
  private LocalDate fechaDevolucionEstimada;
  private EstadoPrestamo estadoPrestamo;
  private Boolean vencido;

  public Long getId_prestamo() {
    return id_prestamo;
  }

  public void setId_prestamo(Long id_prestamo) {
    this.id_prestamo = id_prestamo;
  }

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

  public EstadoPrestamo getEstadoPrestamo() {
    return estadoPrestamo;
  }

  public void setEstadoPrestamo(EstadoPrestamo estadoPrestamo) {
    this.estadoPrestamo = estadoPrestamo;
  }

  public Boolean getVencido() {
    return vencido;
  }

  public void setVencido(Boolean vencido) {
    this.vencido = vencido;
  }

}
