package com.proyecto.fhce.library.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.MotivoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;

// ── REQUEST: crear sanción manual (daño o pérdida) ────────────────────────
public class SancionDTO {

    public record SancionManualRequestDTO(
            @NotNull(message = "El ID de usuario es requerido") Long usuarioId,

            @NotNull(message = "El ID de biblioteca es requerido") Long bibliotecaId,

            // Para sanciones manuales el préstamo puede ser opcional (ej: daño en sala)
            Long prestamoId,

            @NotNull(message = "El motivo es requerido") MotivoSancion motivo,

            @DecimalMin(value = "0.0", message = "El monto no puede ser negativo") BigDecimal montoFijo,

            String observaciones) {
    }

    // ── REQUEST: registrar pago de multa ──────────────────────────────────
    public record PagoMultaRequestDTO(
            @NotNull(message = "El método de pago es requerido") String metodoPago,

            String observaciones) {
    }

    // ── REQUEST: condonar sanción (solo ADMIN) ────────────────────────────
    public record CondonacionRequestDTO(
            @NotNull(message = "El motivo de condonación es requerido") String observaciones) {
    }

    // ── RESPONSE: sanción completa ────────────────────────────────────────
    public record SancionResponseDTO(
            Long idSancion,
            Long usuarioId,
            String nombreUsuario,
            String ciUsuario,
            Long prestamoId,
            Long bibliotecaId,
            String nombreBiblioteca,
            TipoSancion tipoSancion,
            MotivoSancion motivo,
            EstadoSancion estado,
            Integer diasRetraso,
            BigDecimal montoMulta,
            Integer diasSuspension,
            LocalDateTime fechaGeneracion,
            LocalDate fechaInicioSuspension,
            LocalDate fechaFinSuspension,
            LocalDateTime fechaPago,
            LocalDateTime fechaCondonacion,
            String metodoPago,
            String observaciones,
            boolean suspensionVigente) {
    }

    // ── RESPONSE: resumen para incluir en respuestas de préstamo/usuario ──
    public record SancionResumenDTO(
            Long idSancion,
            Long idPrestamo,
            TipoSancion tipoSancion,
            EstadoSancion estado,
            BigDecimal montoMulta,
            LocalDate fechaFinSuspension,
            boolean suspensionVigente,
            String motivo) {
    }

    // ── RESPONSE: estado de sanciones de un usuario (para bloqueos) ──────
    public record EstadoSancionUsuarioDTO(
            Long usuarioId,
            boolean tieneSuspensionVigente,
            boolean tieneDeudaPendiente,
            long totalSancionesActivas,
            BigDecimal montoTotalDeuda,
            LocalDate fechaFinSuspensionMasProxima,
            List<SancionResumenDTO> sanciones) {
    }
}