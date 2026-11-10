package com.proyecto.fhce.library.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.TipoPrestamo;

/**
 * Consultas AGREGADAS para gráficos. Devuelven filas Object[] ya agrupadas
 * (la base de datos cuenta; Java solo da forma), nunca entidades.
 *
 * Reglas comunes:
 * - bibliotecaId null = todas las bibliotecas.
 * - desde / hasta NUNCA son null (el service pasa un rango abierto):
 * PostgreSQL no infiere el tipo de un timestamp null en "? IS NULL".
 * - hasta es EXCLUSIVO (00:00 del día siguiente).
 */
@org.springframework.stereotype.Repository
public interface ReporteGraficoRepository extends Repository<Prestamo, Long> {

  /** [anio:Integer, mes:Integer, cantidad:Long] ordenado cronológicamente. */
  @Query("""
      SELECT year(p.fechaPrestamo), month(p.fechaPrestamo), COUNT(p)
      FROM Prestamo p
      WHERE (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
        AND (:estado IS NULL OR p.estadoPrestamo = :estado)
        AND (:tipo IS NULL OR p.tipoPrestamo = :tipo)
        AND p.fechaPrestamo >= :desde
        AND p.fechaPrestamo < :hasta
      GROUP BY year(p.fechaPrestamo), month(p.fechaPrestamo)
      ORDER BY year(p.fechaPrestamo), month(p.fechaPrestamo)
      """)
  List<Object[]> prestamosPorMes(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estado") EstadoPrestamo estado,
      @Param("tipo") TipoPrestamo tipo,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  /** [estado:EstadoPrestamo, cantidad:Long] */
  @Query("""
      SELECT p.estadoPrestamo, COUNT(p)
      FROM Prestamo p
      WHERE (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
        AND (:tipo IS NULL OR p.tipoPrestamo = :tipo)
        AND p.fechaPrestamo >= :desde
        AND p.fechaPrestamo < :hasta
      GROUP BY p.estadoPrestamo
      """)
  List<Object[]> prestamosPorEstado(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("tipo") TipoPrestamo tipo,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  /** [tipo:TipoPrestamo, cantidad:Long] */
  @Query("""
      SELECT p.tipoPrestamo, COUNT(p)
      FROM Prestamo p
      WHERE (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
        AND (:estado IS NULL OR p.estadoPrestamo = :estado)
        AND p.fechaPrestamo >= :desde
        AND p.fechaPrestamo < :hasta
      GROUP BY p.tipoPrestamo
      """)
  List<Object[]> prestamosPorTipo(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("estado") EstadoPrestamo estado,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  /**
   * [titulo:String, cantidad:Long] más prestados primero. Usar Pageable para el
   * top N.
   */
  @Query("""
      SELECT l.titulo, COUNT(p)
      FROM Prestamo p
      JOIN p.ejemplar e
      JOIN e.edicion ed
      JOIN ed.libro l
      WHERE (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
        AND p.fechaPrestamo >= :desde
        AND p.fechaPrestamo < :hasta
      GROUP BY l.idLibro, l.titulo
      ORDER BY COUNT(p) DESC, l.titulo ASC
      """)
  List<Object[]> librosMasPrestados(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta,
      Pageable pageable);

  /**
   * [titulo:String, cantidad:Long] menos prestados primero.
   * Solo considera libros con al menos un préstamo; los que NUNCA se prestaron
   * están en el reporte "ejemplares sin circulación".
   */
  @Query("""
      SELECT l.titulo, COUNT(p)
      FROM Prestamo p
      JOIN p.ejemplar e
      JOIN e.edicion ed
      JOIN ed.libro l
      WHERE (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
        AND p.fechaPrestamo >= :desde
        AND p.fechaPrestamo < :hasta
      GROUP BY l.idLibro, l.titulo
      ORDER BY COUNT(p) ASC, l.titulo ASC
      """)
  List<Object[]> librosMenosPrestados(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta,
      Pageable pageable);

  /**
   * [clasificacionDecimal:String, cantidad:Long]. Se agrupa por el código
   * completo y el service lo reduce a la clase Dewey (primer dígito) en Java:
   * evita funciones de texto en GROUP BY.
   */
  @Query("""
      SELECT e.clasificacionDecimal, COUNT(p)
      FROM Prestamo p
      JOIN p.ejemplar e
      WHERE (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
        AND p.fechaPrestamo >= :desde
        AND p.fechaPrestamo < :hasta
      GROUP BY e.clasificacionDecimal
      """)
  List<Object[]> prestamosPorClasificacion(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  /** [nombreBiblioteca:String, estado:EstadoEjemplar, cantidad:Long] */
  @Query("""
      SELECT b.nombre, e.estadoEjemplar, COUNT(e)
      FROM Ejemplar e
      JOIN e.biblioteca b
      WHERE (:bibliotecaId IS NULL OR b.idBiblioteca = :bibliotecaId)
      GROUP BY b.idBiblioteca, b.nombre, e.estadoEjemplar
      ORDER BY b.nombre
      """)
  List<Object[]> ejemplaresPorBibliotecaYEstado(@Param("bibliotecaId") Long bibliotecaId);

  /**
   * [fechaDevolucionEstimada:LocalDate, cantidad:Long] de préstamos VENCIDOS
   * (mismo criterio que el reporte "devoluciones pendientes"). El service
   * reparte las fechas en tramos de retraso.
   */
  @Query("""
      SELECT p.fechaDevolucionEstimada, COUNT(p)
      FROM Prestamo p
      WHERE p.estadoPrestamo = 'VENCIDO'
        AND (:bibliotecaId IS NULL OR p.biblioteca.idBiblioteca = :bibliotecaId)
      GROUP BY p.fechaDevolucionEstimada
      """)
  List<Object[]> vencidosPorFechaEstimada(@Param("bibliotecaId") Long bibliotecaId);

  /**
   * [estado:EstadoSancion, cantidad:Long, monto:BigDecimal] por fechaGeneracion.
   */
  @Query("""
      SELECT s.estado, COUNT(s), COALESCE(SUM(s.montoMulta), 0)
      FROM Sancion s
      WHERE (:bibliotecaId IS NULL OR s.biblioteca.idBiblioteca = :bibliotecaId)
        AND s.fechaGeneracion >= :desde
        AND s.fechaGeneracion < :hasta
      GROUP BY s.estado
      """)
  List<Object[]> sancionesPorEstado(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  /** [tipo:TipoSancion, cantidad:Long] por fechaGeneracion. */
  @Query("""
      SELECT s.tipoSancion, COUNT(s)
      FROM Sancion s
      WHERE (:bibliotecaId IS NULL OR s.biblioteca.idBiblioteca = :bibliotecaId)
        AND s.fechaGeneracion >= :desde
        AND s.fechaGeneracion < :hasta
      GROUP BY s.tipoSancion
      """)
  List<Object[]> sancionesPorTipo(
      @Param("bibliotecaId") Long bibliotecaId,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);
}