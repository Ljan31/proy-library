package com.proyecto.fhce.library.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "audit_logs")
public class Auditoria {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id_auditoria;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id")
  private Usuario usuario;

  @Column(nullable = false, length = 100)
  private String accion;

  @Column(name = "tabla_afectada", nullable = false, length = 100)
  private String tablaAfectada;

  private Long id_registro;

  @Column(name = "valores_anteriores", columnDefinition = "TEXT")
  private String valoresAnteriores;

  @Column(name = "valores_nuevos", columnDefinition = "TEXT")
  private String valoresNuevos;

  @Column(name = "fecha_hora", nullable = false)
  private LocalDateTime fechaHora;

  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @PrePersist
  protected void onCreate() {
    fechaHora = LocalDateTime.now();
  }

  public Long getId_auditoria() {
    return id_auditoria;
  }

  public void setId_auditoria(Long id_auditoria) {
    this.id_auditoria = id_auditoria;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public String getAccion() {
    return accion;
  }

  public void setAccion(String accion) {
    this.accion = accion;
  }

  public String getTablaAfectada() {
    return tablaAfectada;
  }

  public void setTablaAfectada(String tablaAfectada) {
    this.tablaAfectada = tablaAfectada;
  }

  public Long getId_registro() {
    return id_registro;
  }

  public void setId_registro(Long id_registro) {
    this.id_registro = id_registro;
  }

  public String getValoresAnteriores() {
    return valoresAnteriores;
  }

  public void setValoresAnteriores(String valoresAnteriores) {
    this.valoresAnteriores = valoresAnteriores;
  }

  public String getValoresNuevos() {
    return valoresNuevos;
  }

  public void setValoresNuevos(String valoresNuevos) {
    this.valoresNuevos = valoresNuevos;
  }

  public LocalDateTime getFechaHora() {
    return fechaHora;
  }

  public void setFechaHora(LocalDateTime fechaHora) {
    this.fechaHora = fechaHora;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

}