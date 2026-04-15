package com.proyecto.fhce.library.dto.response.loads;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.proyecto.fhce.library.dto.response.library.BibliotecaSimpleResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.enums.CondicionFisicaLibro;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.TipoDocumentoGarantia;

public class PrestamoResponse {
  private Long id_prestamo;
  private EjemplarResponse ejemplar;
  private UsuarioSimpleResponse usuario;
  private BibliotecaSimpleResponse biblioteca;
  private LocalDateTime fechaPrestamo;
  private LocalDate fechaDevolucionEstimada;
  private LocalDateTime fechaDevolucionReal;
  private UsuarioSimpleResponse bibliotecarioPrestamo;
  private UsuarioSimpleResponse bibliotecarioDevolucion;
  private EstadoPrestamo estadoPrestamo;
  private String observaciones;
  private Integer renovaciones;
  private Integer diasVencidos; // computed
  private Boolean vencido; // computed
  private TipoDocumentoGarantia tipoDocumentoGarantia;
  private CondicionFisicaLibro condicionEntrega;
  private CondicionFisicaLibro condicionDevolucion;
  // private SancionResponse sancion;

  public Long getId_prestamo() {
    return id_prestamo;
  }

  public void setId_prestamo(Long id_prestamo) {
    this.id_prestamo = id_prestamo;
  }

  public EjemplarResponse getEjemplar() {
    return ejemplar;
  }

  public void setEjemplar(EjemplarResponse ejemplar) {
    this.ejemplar = ejemplar;
  }

  public UsuarioSimpleResponse getUsuario() {
    return usuario;
  }

  public void setUsuario(UsuarioSimpleResponse usuario) {
    this.usuario = usuario;
  }

  public BibliotecaSimpleResponse getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(BibliotecaSimpleResponse biblioteca) {
    this.biblioteca = biblioteca;
  }

  public LocalDateTime getFechaPrestamo() {
    return fechaPrestamo;
  }

  public void setFechaPrestamo(LocalDateTime fechaPrestamo) {
    this.fechaPrestamo = fechaPrestamo;
  }

  public LocalDate getFechaDevolucionEstimada() {
    return fechaDevolucionEstimada;
  }

  public void setFechaDevolucionEstimada(LocalDate fechaDevolucionEstimada) {
    this.fechaDevolucionEstimada = fechaDevolucionEstimada;
  }

  public LocalDateTime getFechaDevolucionReal() {
    return fechaDevolucionReal;
  }

  public void setFechaDevolucionReal(LocalDateTime fechaDevolucionReal) {
    this.fechaDevolucionReal = fechaDevolucionReal;
  }

  public UsuarioSimpleResponse getBibliotecarioPrestamo() {
    return bibliotecarioPrestamo;
  }

  public void setBibliotecarioPrestamo(UsuarioSimpleResponse bibliotecarioPrestamo) {
    this.bibliotecarioPrestamo = bibliotecarioPrestamo;
  }

  public UsuarioSimpleResponse getBibliotecarioDevolucion() {
    return bibliotecarioDevolucion;
  }

  public void setBibliotecarioDevolucion(UsuarioSimpleResponse bibliotecarioDevolucion) {
    this.bibliotecarioDevolucion = bibliotecarioDevolucion;
  }

  public EstadoPrestamo getEstadoPrestamo() {
    return estadoPrestamo;
  }

  public void setEstadoPrestamo(EstadoPrestamo estadoPrestamo) {
    this.estadoPrestamo = estadoPrestamo;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public Integer getRenovaciones() {
    return renovaciones;
  }

  public void setRenovaciones(Integer renovaciones) {
    this.renovaciones = renovaciones;
  }

  public Integer getDiasVencidos() {
    return diasVencidos;
  }

  public void setDiasVencidos(Integer diasVencidos) {
    this.diasVencidos = diasVencidos;
  }

  public Boolean getVencido() {
    return vencido;
  }

  public void setVencido(Boolean vencido) {
    this.vencido = vencido;
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

  public CondicionFisicaLibro getCondicionDevolucion() {
    return condicionDevolucion;
  }

  public void setCondicionDevolucion(CondicionFisicaLibro condicionDevolucion) {
    this.condicionDevolucion = condicionDevolucion;
  }

}
