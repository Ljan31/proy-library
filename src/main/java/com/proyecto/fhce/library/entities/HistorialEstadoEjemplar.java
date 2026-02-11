package com.proyecto.fhce.library.entities;

import java.time.LocalDateTime;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

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
@Table(name = "ejemplar_status_history")
public class HistorialEstadoEjemplar {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_historial")
  private Long idHistorial;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ejemplar_id", nullable = false)
  private Ejemplar ejemplar;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_anterior")
  private EstadoEjemplar estadoAnterior;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado_nuevo", nullable = false)
  private EstadoEjemplar estadoNuevo;

  @Column(name = "fecha_cambio", nullable = false)
  private LocalDateTime fechaCambio;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_change_id")
  private Usuario usuarioCambio;

  @Column(columnDefinition = "TEXT")
  private String motivo;

  @PrePersist
  protected void onCreate() {
    fechaCambio = LocalDateTime.now();
  }

  public Long getIdHistorial() {
    return idHistorial;
  }

  public void setIdHistorial(Long idHistorial) {
    this.idHistorial = idHistorial;
  }

  public Ejemplar getEjemplar() {
    return ejemplar;
  }

  public void setEjemplar(Ejemplar ejemplar) {
    this.ejemplar = ejemplar;
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

  public Usuario getUsuarioCambio() {
    return usuarioCambio;
  }

  public void setUsuarioCambio(Usuario usuarioCambio) {
    this.usuarioCambio = usuarioCambio;
  }

  public String getMotivo() {
    return motivo;
  }

  public void setMotivo(String motivo) {
    this.motivo = motivo;
  }

}