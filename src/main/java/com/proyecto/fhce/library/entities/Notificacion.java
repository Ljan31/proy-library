package com.proyecto.fhce.library.entities;

import java.time.LocalDateTime;

import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;
import com.proyecto.fhce.library.enums.notificaciones.EstadoEnvio;
import com.proyecto.fhce.library.enums.notificaciones.TipoNotificacion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificaciones", indexes = {
        @Index(name = "idx_notif_usuario", columnList = "id_usuario"),
        @Index(name = "idx_notif_estado_envio", columnList = "estado_envio"),
        @Index(name = "idx_notif_tipo", columnList = "tipo_notificacion")
})
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_notificacion", nullable = false, length = 30)
    private TipoNotificacion tipoNotificacion;

    @Column(name = "asunto", nullable = false, length = 200)
    private String asunto;

    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String mensaje;

    @Column(name = "fecha_envio")
    private LocalDateTime fechaEnvio;

    @Column(name = "fecha_lectura")
    private LocalDateTime fechaLectura;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_envio", nullable = false, length = 10)
    private EstadoEnvio estadoEnvio;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal", nullable = false, length = 10)
    private CanalNotificacion canal;

    @Column(name = "id_referencia")
    private Long idReferencia;

    @Column(name = "tipo_referencia", length = 20)
    private String tipoReferencia;

    @Column(name = "intentos_envio", nullable = false)
    private Integer intentosEnvio;

    @Column(name = "ultimo_error", columnDefinition = "TEXT")
    private String ultimoError;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    // ========================
    // CONSTRUCTORES
    // ========================

    public Notificacion() {
    }

    public Notificacion(Long idNotificacion, Long idUsuario, TipoNotificacion tipoNotificacion,
            String asunto, String mensaje, LocalDateTime fechaEnvio,
            LocalDateTime fechaLectura, EstadoEnvio estadoEnvio,
            CanalNotificacion canal, Long idReferencia, String tipoReferencia,
            Integer intentosEnvio, String ultimoError, LocalDateTime fechaCreacion) {
        this.idNotificacion = idNotificacion;
        this.idUsuario = idUsuario;
        this.tipoNotificacion = tipoNotificacion;
        this.asunto = asunto;
        this.mensaje = mensaje;
        this.fechaEnvio = fechaEnvio;
        this.fechaLectura = fechaLectura;
        this.estadoEnvio = estadoEnvio;
        this.canal = canal;
        this.idReferencia = idReferencia;
        this.tipoReferencia = tipoReferencia;
        this.intentosEnvio = intentosEnvio;
        this.ultimoError = ultimoError;
        this.fechaCreacion = fechaCreacion;
    }

    // ========================
    // GETTERS Y SETTERS
    // ========================

    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public void setIdNotificacion(Long idNotificacion) {
        this.idNotificacion = idNotificacion;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public TipoNotificacion getTipoNotificacion() {
        return tipoNotificacion;
    }

    public void setTipoNotificacion(TipoNotificacion tipoNotificacion) {
        this.tipoNotificacion = tipoNotificacion;
    }

    public String getAsunto() {
        return asunto;
    }

    public void setAsunto(String asunto) {
        this.asunto = asunto;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public LocalDateTime getFechaLectura() {
        return fechaLectura;
    }

    public void setFechaLectura(LocalDateTime fechaLectura) {
        this.fechaLectura = fechaLectura;
    }

    public EstadoEnvio getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(EstadoEnvio estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public CanalNotificacion getCanal() {
        return canal;
    }

    public void setCanal(CanalNotificacion canal) {
        this.canal = canal;
    }

    public Long getIdReferencia() {
        return idReferencia;
    }

    public void setIdReferencia(Long idReferencia) {
        this.idReferencia = idReferencia;
    }

    public String getTipoReferencia() {
        return tipoReferencia;
    }

    public void setTipoReferencia(String tipoReferencia) {
        this.tipoReferencia = tipoReferencia;
    }

    public Integer getIntentosEnvio() {
        return intentosEnvio;
    }

    public void setIntentosEnvio(Integer intentosEnvio) {
        this.intentosEnvio = intentosEnvio;
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public void setUltimoError(String ultimoError) {
        this.ultimoError = ultimoError;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    // ========================
    // CICLO DE VIDA
    // ========================

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();

        if (this.estadoEnvio == null) {
            this.estadoEnvio = EstadoEnvio.PENDIENTE;
        }
        if (this.intentosEnvio == null) {
            this.intentosEnvio = 0;
        }
    }

    // ========================
    // LÓGICA DE DOMINIO
    // ========================

    public boolean puedeReintentar(int maxIntentos) {
        return this.estadoEnvio == EstadoEnvio.FALLIDO
                && this.intentosEnvio < maxIntentos;
    }

    public boolean esPendiente() {
        return this.estadoEnvio == EstadoEnvio.PENDIENTE;
    }

    public boolean fueLeida() {
        return this.fechaLectura != null;
    }

    public void registrarEnvioExitoso() {
        this.estadoEnvio = EstadoEnvio.ENVIADO;
        this.fechaEnvio = LocalDateTime.now();
        this.ultimoError = null;
    }

    public void registrarFallo(String mensajeError) {
        this.estadoEnvio = EstadoEnvio.FALLIDO;
        this.intentosEnvio = (this.intentosEnvio == null ? 1 : this.intentosEnvio + 1);
        this.ultimoError = mensajeError;
    }

    public void marcarComoLeida() {
        if (this.fechaLectura == null) {
            this.fechaLectura = LocalDateTime.now();
        }
    }
}
