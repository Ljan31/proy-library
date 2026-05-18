
package com.proyecto.fhce.library.dto.response.library;

import com.proyecto.fhce.library.enums.EstadoReserva;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de salida para el módulo de Reservas.
 *
 * NUNCA se expone la entidad Reserva directamente.
 * Este DTO aplana los datos necesarios para el frontend
 * sin exponer el grafo de entidades JPA.
 */
public class ReservaResponse {

  private Long idReserva;

  // Datos del usuario
  private Long usuarioId;
  private String usuarioNombreCompleto;

  // Datos del libro
  private Long libroId;
  private String libroTitulo;

  // Datos del ejemplar asignado (null mientras está ACTIVA)
  private Long ejemplarId;
  private String ejemplarCodigo;

  // Datos de la biblioteca
  private Long bibliotecaId;
  private String bibliotecaNombre;

  // Datos del préstamo generado (null hasta ATENDIDA)
  private Long prestamoId;

  // Datos de la reserva
  private LocalDateTime fechaReserva;
  private LocalDate fechaVencimientoReserva;
  private EstadoReserva estadoReserva;
  private Integer prioridad;
  private String observaciones;

  public Long getIdReserva() {
    return idReserva;
  }

  public void setIdReserva(Long idReserva) {
    this.idReserva = idReserva;
  }

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public String getUsuarioNombreCompleto() {
    return usuarioNombreCompleto;
  }

  public void setUsuarioNombreCompleto(String usuarioNombreCompleto) {
    this.usuarioNombreCompleto = usuarioNombreCompleto;
  }

  public Long getLibroId() {
    return libroId;
  }

  public void setLibroId(Long libroId) {
    this.libroId = libroId;
  }

  public String getLibroTitulo() {
    return libroTitulo;
  }

  public void setLibroTitulo(String libroTitulo) {
    this.libroTitulo = libroTitulo;
  }

  public Long getEjemplarId() {
    return ejemplarId;
  }

  public void setEjemplarId(Long ejemplarId) {
    this.ejemplarId = ejemplarId;
  }

  public String getEjemplarCodigo() {
    return ejemplarCodigo;
  }

  public void setEjemplarCodigo(String ejemplarCodigo) {
    this.ejemplarCodigo = ejemplarCodigo;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getBibliotecaNombre() {
    return bibliotecaNombre;
  }

  public void setBibliotecaNombre(String bibliotecaNombre) {
    this.bibliotecaNombre = bibliotecaNombre;
  }

  public Long getPrestamoId() {
    return prestamoId;
  }

  public void setPrestamoId(Long prestamoId) {
    this.prestamoId = prestamoId;
  }

  public LocalDateTime getFechaReserva() {
    return fechaReserva;
  }

  public void setFechaReserva(LocalDateTime fechaReserva) {
    this.fechaReserva = fechaReserva;
  }

  public LocalDate getFechaVencimientoReserva() {
    return fechaVencimientoReserva;
  }

  public void setFechaVencimientoReserva(LocalDate fechaVencimientoReserva) {
    this.fechaVencimientoReserva = fechaVencimientoReserva;
  }

  public EstadoReserva getEstadoReserva() {
    return estadoReserva;
  }

  public void setEstadoReserva(EstadoReserva estadoReserva) {
    this.estadoReserva = estadoReserva;
  }

  public Integer getPrioridad() {
    return prioridad;
  }

  public void setPrioridad(Integer prioridad) {
    this.prioridad = prioridad;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }
}
