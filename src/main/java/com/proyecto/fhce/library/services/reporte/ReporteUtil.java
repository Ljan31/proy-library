package com.proyecto.fhce.library.services.reporte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.CondicionFisicaLibro;
import com.proyecto.fhce.library.exception.BadRequestException;

/**
 * Utilidades compartidas por los services de reportes.
 * Centraliza rangos de fechas, nombres y cálculos que antes estaban
 * duplicados en cada mapper.
 */
final class ReporteUtil {

  private ReporteUtil() {
  }

  /**
   * Límites del rango abierto cuando el usuario no envía fechas (= todos los
   * registros).
   */
  private static final LocalDateTime MIN = LocalDateTime.of(1900, 1, 1, 0, 0);
  private static final LocalDateTime MAX = LocalDateTime.of(2999, 12, 31, 0, 0);

  /**
   * Inicio del rango: 00:00:00 del día indicado, o 1900-01-01 si no hay fecha.
   * Nunca devuelve null: PostgreSQL no puede inferir el tipo de un parámetro
   * temporal null en "? IS NULL OR campo >= ?" y la query falla.
   */
  static LocalDateTime desde(LocalDate fecha) {
    return fecha == null ? MIN : fecha.atStartOfDay();
  }

  /**
   * Fin EXCLUSIVO del rango: 00:00:00 del día siguiente, o 2999-12-31 si no hay
   * fecha. Usar con "<" en las queries; así no se pierde ningún instante del
   * último día (atTime(23, 59, 59) dejaba fuera el último segundo).
   */
  static LocalDateTime hastaExclusivo(LocalDate fecha) {
    return fecha == null ? MAX : fecha.plusDays(1).atStartOfDay();
  }

  static void validarRango(LocalDate inicio, LocalDate fin) {
    if (inicio != null && fin != null && inicio.isAfter(fin)) {
      throw new BadRequestException("fechaInicio no puede ser posterior a fechaFin");
    }
  }

  /** "Nombre ApellidoPaterno", tolerante a nulls. */
  static String nombreCorto(Persona persona) {
    if (persona == null) {
      return "";
    }
    String nombre = persona.getNombre() != null ? persona.getNombre() : "";
    String apellido = persona.getApellido_pat() != null ? persona.getApellido_pat() : "";
    return (nombre + " " + apellido).trim();
  }

  /**
   * Días de retraso de un préstamo (0 si no hay retraso).
   * Si ya fue devuelto se compara contra la fecha real; si no, contra hoy.
   * Reemplaza las tres versiones distintas que existían en los mappers.
   */
  static int diasRetraso(Prestamo p, LocalDate hoy) {
    LocalDate estimada = p.getFechaDevolucionEstimada();
    if (estimada == null) {
      return 0;
    }
    LocalDate referencia = p.getFechaDevolucionReal() != null
        ? p.getFechaDevolucionReal().toLocalDate()
        : hoy;
    long dias = ChronoUnit.DAYS.between(estimada, referencia);
    return dias > 0 ? (int) dias : 0;
  }

  /**
   * Diferencia de condición física entre devolución y entrega.
   * Positivo = deterioro, 0 = sin cambio, negativo = mejora.
   * Devuelve null si falta alguna de las dos condiciones (no se puede comparar).
   * Nota: depende del orden de declaración del enum CondicionFisicaLibro
   * (de mejor a peor). Si se reordena el enum, hay que revisar esto.
   */
  static Integer diferenciaCondicion(CondicionFisicaLibro entrega, CondicionFisicaLibro devolucion) {
    if (entrega == null || devolucion == null) {
      return null;
    }
    return devolucion.ordinal() - entrega.ordinal();
  }
}