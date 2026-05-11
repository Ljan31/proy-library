package com.proyecto.fhce.library.entities;

import com.proyecto.fhce.library.enums.EstadoReserva;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad principal del módulo de Reservas.
 *
 * Diseño:
 * - El usuario reserva un LIBRO (no un ejemplar). El ejemplar se asigna
 * automáticamente cuando hay disponibilidad (ejemplar_id puede ser null).
 * - La prioridad define el orden FIFO de atención en la cola.
 * - idConfigUsado guarda la configuración vigente al momento de crear la
 * reserva,
 * para calcular correctamente la fecha de vencimiento incluso si la config
 * cambia.
 * - prestamo_id se llena solo cuando la reserva es convertida a préstamo
 * (estado ATENDIDA).
 */
@Entity
@Table(name = "reservas", indexes = {
    @Index(name = "idx_reserva_usuario", columnList = "user_id"),
    @Index(name = "idx_reserva_libro", columnList = "libro_id"),
    @Index(name = "idx_reserva_estado", columnList = "estado_reserva"),
    @Index(name = "idx_reserva_biblioteca", columnList = "library_id")
})
public class Reserva {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_reserva")
  private Long idReserva;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private Usuario usuario;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "libro_id", nullable = false)
  private Libro libro;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "library_id", nullable = false)
  private Biblioteca biblioteca;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ejemplar_id")
  private Ejemplar ejemplar;

  /**
   * Préstamo generado cuando el usuario retira el libro.
   * NULL hasta que el estado cambia a ATENDIDA.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prestamo_id")
  private Prestamo prestamo;

  @Column(name = "fecha_reserva", nullable = false)
  private LocalDateTime fechaReserva;

  @Column(name = "fecha_vencimiento_reserva")
  private LocalDate fechaVencimientoReserva;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_reserva", nullable = false, length = 20)
  private EstadoReserva estadoReserva;

  /**
   * Posición en la cola de espera para este libro/biblioteca.
   * Valor menor = mayor prioridad (FIFO).
   * Se asigna automáticamente como MAX(prioridad) + 1 al crear la reserva.
   */
  @Column(name = "prioridad", nullable = false)
  private Integer prioridad;

  /**
   * ID de la configuración de préstamo vigente al momento de crear la reserva.
   * Se usa para calcular fechas de vencimiento con la config original,
   * aunque las reglas de la biblioteca cambien posteriormente.
   */
  @Column(name = "id_config_usado")
  private Long idConfigUsado;

  @Column(columnDefinition = "TEXT")
  private String observaciones;

  public Reserva() {
  }

  public Long getIdReserva() {
    return idReserva;
  }

  public void setIdReserva(Long idReserva) {
    this.idReserva = idReserva;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Libro getLibro() {
    return libro;
  }

  public void setLibro(Libro libro) {
    this.libro = libro;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Ejemplar getEjemplar() {
    return ejemplar;
  }

  public void setEjemplar(Ejemplar ejemplar) {
    this.ejemplar = ejemplar;
  }

  public Prestamo getPrestamo() {
    return prestamo;
  }

  public void setPrestamo(Prestamo prestamo) {
    this.prestamo = prestamo;
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

  public Long getIdConfigUsado() {
    return idConfigUsado;
  }

  public void setIdConfigUsado(Long idConfigUsado) {
    this.idConfigUsado = idConfigUsado;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }
}