package com.proyecto.fhce.library.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;

import java.util.List;
import java.util.Optional;

@Repository
public interface SancionRepository extends JpaRepository<Sancion, Long> {

  // ── Verificación de duplicados ────────────────────────────────────────────

  /**
   * Verifica si ya existe una sanción activa para un préstamo específico.
   * Previene la generación de sanciones duplicadas por el mismo préstamo.
   */
  boolean existsByPrestamo_IdPrestamoAndEstado(Long idPrestamo, EstadoSancion estado);

  /**
   * Cuenta sanciones activas de un usuario (sin importar tipo).
   * Útil para verificar bloqueos antes de préstamos o reservas.
   */
  @Query("SELECT COUNT(s) FROM Sancion s WHERE s.usuario.idUsuario = :usuarioId AND s.estado = 'ACTIVA'")
  long contarSancionesActivasPorUsuario(@Param("usuarioId") Long usuarioId);

  /**
   * Verifica si un usuario tiene suspensión vigente hoy.
   * Se usa como guarda en préstamos y reservas.
   */
  @Query("""
      SELECT COUNT(s) > 0 FROM Sancion s
      WHERE s.usuario.idUsuario = :usuarioId
        AND s.estado = 'ACTIVA'
        AND (s.tipoSancion = 'SUSPENSION' OR s.tipoSancion = 'MULTA_Y_SUSPENSION')
        AND s.fechaFinSuspension >= CURRENT_DATE
      """)
  boolean tieneSuspensionVigente(@Param("usuarioId") Long usuarioId);

  // ── Consultas por usuario ─────────────────────────────────────────────────

  /**
   * Historial completo de sanciones de un usuario con datos de préstamo y
   * biblioteca.
   */
  @Query("""
      SELECT s FROM Sancion s
      LEFT JOIN FETCH s.prestamo p
      LEFT JOIN FETCH s.biblioteca b
      WHERE s.usuario.idUsuario = :usuarioId
      ORDER BY s.fechaGeneracion DESC
      """)
  List<Sancion> findHistorialByUsuario(@Param("usuarioId") Long usuarioId);

  /**
   * Sanciones activas de un usuario con datos cargados.
   */
  @Query("""
      SELECT s FROM Sancion s
      LEFT JOIN FETCH s.prestamo
      LEFT JOIN FETCH s.biblioteca
      WHERE s.usuario.idUsuario = :usuarioId
        AND s.estado = 'ACTIVA'
      ORDER BY s.fechaGeneracion DESC
      """)
  List<Sancion> findActivasByUsuario(@Param("usuarioId") Long usuarioId);

  // ── Consultas por biblioteca ──────────────────────────────────────────────

  /**
   * Sanciones activas de una biblioteca, con usuario y préstamo cargados.
   * Para el panel del bibliotecario.
   */
  @Query("""
      SELECT s FROM Sancion s
      JOIN FETCH s.usuario u
      JOIN FETCH u.persona
      LEFT JOIN FETCH s.prestamo
      WHERE s.biblioteca.idBiblioteca = :bibliotecaId
        AND s.estado = :estado
      ORDER BY s.fechaGeneracion DESC
      """)
  List<Sancion> findByBibliotecaAndEstado(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estado") EstadoSancion estado);

  // ── Para el módulo de certificados ───────────────────────────────────────

  /**
   * Verifica si el usuario tiene alguna sanción activa con monto pendiente.
   * El módulo de certificados de no adeudo consulta esto.
   */
  @Query("""
      SELECT COUNT(s) > 0 FROM Sancion s
      WHERE s.usuario.idUsuario = :usuarioId
        AND s.estado = 'ACTIVA'
        AND (s.montoMulta IS NULL OR s.montoMulta > 0)
      """)
  boolean tieneDeudaPendiente(@Param("usuarioId") Long usuarioId);

  // ── Para el CRON de actualización de multas (CRON 2 — 02:30 AM) ──────────

  /**
   * Devuelve sanciones ACTIVAS originadas por retraso (tienen préstamo y config),
   * con el préstamo y su fecha estimada cargados para calcular días actuales.
   *
   * Excluye sanciones manuales (sin prestamo o sin idConfigUsado) porque
   * su monto es fijo — no se actualiza diariamente.
   *
   * El CRON de actualización itera esta lista y recalcula el monto de cada una.
   */
  @Query("""
      SELECT s FROM Sancion s
      JOIN FETCH s.prestamo p
      JOIN FETCH s.usuario u
      WHERE s.estado = 'ACTIVA'
        AND s.motivo = 'RETRASO_DEVOLUCION'
        AND s.idConfigUsado IS NOT NULL
        AND p.fechaDevolucionReal IS NULL
      ORDER BY s.fechaGeneracion ASC
      """)
  List<Sancion> findActivasPorRetrasoConConfig();

  // ── Para el CRON de vencimientos ──────────────────────────────────────────

  /**
   * Consulta sanciones activas cuya suspensión ya venció (para limpiarlas
   * automáticamente).
   */
  @Query("""
      SELECT s FROM Sancion s
      WHERE s.estado = 'ACTIVA'
        AND (s.tipoSancion = 'SUSPENSION' OR s.tipoSancion = 'MULTA_Y_SUSPENSION')
        AND s.fechaFinSuspension < CURRENT_DATE
        AND s.montoMulta IS NULL OR s.montoMulta = 0
      """)
  List<Sancion> findSuspensionesVencidasSinDeuda();

  // ── Búsqueda por préstamo ─────────────────────────────────────────────────

  Optional<Sancion> findByPrestamo_IdPrestamo(Long idPrestamo);

  // ── Estadísticas ─────────────────────────────────────────────────────────

  @Query("SELECT COUNT(s) FROM Sancion s WHERE s.biblioteca.idBiblioteca = :bibliotecaId AND s.estado = 'ACTIVA'")
  long contarActivasPorBiblioteca(@Param("bibliotecaId") Long bibliotecaId);

  List<Sancion> findByTipoSancionAndEstado(TipoSancion tipoSancion, EstadoSancion estado);
}