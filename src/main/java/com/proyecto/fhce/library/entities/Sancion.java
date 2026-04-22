package com.proyecto.fhce.library.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.MotivoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "sanciones", indexes = {
    @Index(name = "idx_sancion_usuario", columnList = "usuario_id"),
    @Index(name = "idx_sancion_prestamo", columnList = "prestamo_id"),
    @Index(name = "idx_sancion_estado", columnList = "estado")
})
public class Sancion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_sancion")
  private Long idSancion;

  // ── Usuario sancionado ───────────────────────────────────────────────────
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "usuario_id", nullable = false)
  private Usuario usuario;

  // ── Préstamo que originó la sanción (null si es sanción manual sin préstamo)
  // ──
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "prestamo_id")
  private Prestamo prestamo;

  // ── Biblioteca donde ocurrió ─────────────────────────────────────────────
  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "biblioteca_id", nullable = false)
  private Biblioteca biblioteca;

  // ── Bibliotecario que registró (para sanciones manuales o quien procesó) ─
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bibliotecario_id")
  private Usuario bibliotecario;

  // ── Tipo y motivo ────────────────────────────────────────────────────────
  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "tipo_sancion", nullable = false, length = 30)
  private TipoSancion tipoSancion;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "motivo", nullable = false, length = 30)
  private MotivoSancion motivo;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false, length = 20)
  private EstadoSancion estado = EstadoSancion.ACTIVA;

  // ── Datos del cálculo ────────────────────────────────────────────────────
  @Column(name = "dias_retraso")
  private Integer diasRetraso;

  @DecimalMin(value = "0.0", message = "El monto de la multa no puede ser negativo")
  @Column(name = "monto_multa", precision = 10, scale = 2)
  private BigDecimal montoMulta;

  @Column(name = "dias_suspension")
  private Integer diasSuspension;

  // ── Referencia a la configuración usada ─────────────────────────────────
  // Guardamos idConfig para mantener consistencia histórica aunque la config
  // cambie
  @Column(name = "id_config_usado")
  private Long idConfigUsado;

  // ── Fechas ───────────────────────────────────────────────────────────────
  @NotNull
  @Column(name = "fecha_generacion", nullable = false)
  private LocalDateTime fechaGeneracion;

  @Column(name = "fecha_inicio_suspension")
  private LocalDate fechaInicioSuspension;

  @Column(name = "fecha_fin_suspension")
  private LocalDate fechaFinSuspension;

  @Column(name = "fecha_pago")
  private LocalDateTime fechaPago;

  @Column(name = "fecha_condonacion")
  private LocalDateTime fechaCondonacion;

  // ── Pago ─────────────────────────────────────────────────────────────────
  @Column(name = "metodo_pago", length = 50)
  private String metodoPago;

  // ── Observaciones ────────────────────────────────────────────────────────
  @Column(name = "observaciones", columnDefinition = "TEXT")
  private String observaciones;

  // ─────────────────────────────────────────────────────────────────────────
  // Constructores
  // ─────────────────────────────────────────────────────────────────────────

  public Sancion() {
    this.fechaGeneracion = LocalDateTime.now();
    this.estado = EstadoSancion.ACTIVA;
  }

  // ─────────────────────────────────────────────────────────────────────────
  // Métodos de negocio
  // ─────────────────────────────────────────────────────────────────────────

  public boolean estaActiva() {
    return EstadoSancion.ACTIVA.equals(this.estado);
  }

  public boolean implicaSuspension() {
    return TipoSancion.SUSPENSION.equals(this.tipoSancion)
        || TipoSancion.MULTA_Y_SUSPENSION.equals(this.tipoSancion);
  }

  public boolean suspensionVigente() {
    if (!implicaSuspension() || !estaActiva())
      return false;
    LocalDate hoy = LocalDate.now();
    return fechaFinSuspension != null && !hoy.isAfter(fechaFinSuspension);
  }

  public Long getIdSancion() {
    return idSancion;
  }

  public void setIdSancion(Long idSancion) {
    this.idSancion = idSancion;
  }

  public Usuario getUsuario() {
    return usuario;
  }

  public void setUsuario(Usuario usuario) {
    this.usuario = usuario;
  }

  public Prestamo getPrestamo() {
    return prestamo;
  }

  public void setPrestamo(Prestamo prestamo) {
    this.prestamo = prestamo;
  }

  public Biblioteca getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Biblioteca biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Usuario getBibliotecario() {
    return bibliotecario;
  }

  public void setBibliotecario(Usuario bibliotecario) {
    this.bibliotecario = bibliotecario;
  }

  public TipoSancion getTipoSancion() {
    return tipoSancion;
  }

  public void setTipoSancion(TipoSancion tipoSancion) {
    this.tipoSancion = tipoSancion;
  }

  public MotivoSancion getMotivo() {
    return motivo;
  }

  public void setMotivo(MotivoSancion motivo) {
    this.motivo = motivo;
  }

  public EstadoSancion getEstado() {
    return estado;
  }

  public void setEstado(EstadoSancion estado) {
    this.estado = estado;
  }

  public Integer getDiasRetraso() {
    return diasRetraso;
  }

  public void setDiasRetraso(Integer diasRetraso) {
    this.diasRetraso = diasRetraso;
  }

  public BigDecimal getMontoMulta() {
    return montoMulta;
  }

  public void setMontoMulta(BigDecimal montoMulta) {
    this.montoMulta = montoMulta;
  }

  public Integer getDiasSuspension() {
    return diasSuspension;
  }

  public void setDiasSuspension(Integer diasSuspension) {
    this.diasSuspension = diasSuspension;
  }

  public Long getIdConfigUsado() {
    return idConfigUsado;
  }

  public void setIdConfigUsado(Long idConfigUsado) {
    this.idConfigUsado = idConfigUsado;
  }

  public LocalDateTime getFechaGeneracion() {
    return fechaGeneracion;
  }

  public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
    this.fechaGeneracion = fechaGeneracion;
  }

  public LocalDate getFechaInicioSuspension() {
    return fechaInicioSuspension;
  }

  public void setFechaInicioSuspension(LocalDate fechaInicioSuspension) {
    this.fechaInicioSuspension = fechaInicioSuspension;
  }

  public LocalDate getFechaFinSuspension() {
    return fechaFinSuspension;
  }

  public void setFechaFinSuspension(LocalDate fechaFinSuspension) {
    this.fechaFinSuspension = fechaFinSuspension;
  }

  public LocalDateTime getFechaPago() {
    return fechaPago;
  }

  public void setFechaPago(LocalDateTime fechaPago) {
    this.fechaPago = fechaPago;
  }

  public LocalDateTime getFechaCondonacion() {
    return fechaCondonacion;
  }

  public void setFechaCondonacion(LocalDateTime fechaCondonacion) {
    this.fechaCondonacion = fechaCondonacion;
  }

  public String getMetodoPago() {
    return metodoPago;
  }

  public void setMetodoPago(String metodoPago) {
    this.metodoPago = metodoPago;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

}