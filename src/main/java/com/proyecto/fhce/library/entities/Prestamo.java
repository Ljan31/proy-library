package com.proyecto.fhce.library.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.proyecto.fhce.library.enums.EstadoPrestamo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "loans")
public class Prestamo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id_prestamo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ejemplar_id", nullable = false)
  private Ejemplar ejemplar;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "library_id", nullable = false)
  private Biblioteca biblioteca;

  @Column(name = "fecha_prestamo", nullable = false)
  private LocalDateTime fechaPrestamo;

  @Column(name = "fecha_devolucion_estimada", nullable = false)
  private LocalDate fechaDevolucionEstimada;

  @Column(name = "fecha_devolucion_real")
  private LocalDateTime fechaDevolucionReal;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bibliotecario_prestamo_id")
  private Usuario bibliotecarioPrestamo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bibliotecario_devolucion_id")
  private Usuario bibliotecarioDevolucion;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_prestamo", nullable = false)
  private EstadoPrestamo estadoPrestamo;

  @Column(columnDefinition = "TEXT")
  private String observaciones;

  @Column(nullable = false)
  private Integer renovaciones;

  // @OneToOne(mappedBy = "prestamo", cascade = CascadeType.ALL)
  // private Sancion sancion;

  @PrePersist
  protected void onCreate() {
    fechaPrestamo = LocalDateTime.now();
    if (estadoPrestamo == null)
      estadoPrestamo = EstadoPrestamo.ACTIVO;
    if (renovaciones == null)
      renovaciones = 0;
  }

  public Long getId_prestamo() {
    return id_prestamo;
  }

  public void setId_prestamo(Long id_prestamo) {
    this.id_prestamo = id_prestamo;
  }

  public Ejemplar getEjemplar() {
    return ejemplar;
  }

  public void setEjemplar(Ejemplar ejemplar) {
    this.ejemplar = ejemplar;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
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

  public Usuario getBibliotecarioPrestamo() {
    return bibliotecarioPrestamo;
  }

  public void setBibliotecarioPrestamo(Usuario bibliotecarioPrestamo) {
    this.bibliotecarioPrestamo = bibliotecarioPrestamo;
  }

  public Usuario getBibliotecarioDevolucion() {
    return bibliotecarioDevolucion;
  }

  public void setBibliotecarioDevolucion(Usuario bibliotecarioDevolucion) {
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

}
