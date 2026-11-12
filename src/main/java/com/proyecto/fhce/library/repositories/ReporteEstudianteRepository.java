package com.proyecto.fhce.library.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Reserva;

/**
 * Consultas del dashboard del estudiante. TODAS reciben el usuarioId (que el
 * service obtiene del JWT) y filtran por él: no existe forma de pedir datos
 * de otro usuario.
 */
@org.springframework.stereotype.Repository
public interface ReporteEstudianteRepository extends Repository<Prestamo, Long> {

  @Query("SELECT COUNT(p) FROM Prestamo p WHERE p.usuario.idUsuario = :usuarioId")
  Long countPrestamosDeUsuario(@Param("usuarioId") Long usuarioId);

  /**
   * Préstamos sin devolver (ACTIVO, RENOVADO, VENCIDO), los más urgentes primero.
   */
  @Query("""
      SELECT p FROM Prestamo p
      JOIN FETCH p.biblioteca
      JOIN FETCH p.ejemplar e
      JOIN FETCH e.edicion ed
      JOIN FETCH ed.libro
      WHERE p.usuario.idUsuario = :usuarioId
        AND p.estadoPrestamo IN ('ACTIVO', 'RENOVADO', 'VENCIDO')
      ORDER BY p.fechaDevolucionEstimada ASC, p.idPrestamo ASC
      """)
  List<Prestamo> findPrestamosVigentes(@Param("usuarioId") Long usuarioId);

  /**
   * Reservas ACTIVA y NOTIFICADA; las NOTIFICADAS (listas para retirar) primero.
   */
  @Query("""
      SELECT r FROM Reserva r
      JOIN FETCH r.libro
      JOIN FETCH r.biblioteca
      WHERE r.usuario.idUsuario = :usuarioId
        AND r.estadoReserva IN ('ACTIVA', 'NOTIFICADA')
      ORDER BY CASE WHEN r.estadoReserva = 'NOTIFICADA' THEN 0 ELSE 1 END,
               r.fechaReserva ASC
      """)
  List<Reserva> findReservasVigentes(@Param("usuarioId") Long usuarioId);

  /**
   * [anio:Integer, mes:Integer, cantidad:Long] cronológico. hasta es EXCLUSIVO.
   */
  @Query("""
      SELECT year(p.fechaPrestamo), month(p.fechaPrestamo), COUNT(p)
      FROM Prestamo p
      WHERE p.usuario.idUsuario = :usuarioId
        AND p.fechaPrestamo >= :desde
        AND p.fechaPrestamo < :hasta
      GROUP BY year(p.fechaPrestamo), month(p.fechaPrestamo)
      ORDER BY year(p.fechaPrestamo), month(p.fechaPrestamo)
      """)
  List<Object[]> prestamosPorMes(
      @Param("usuarioId") Long usuarioId,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);
}