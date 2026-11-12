package com.proyecto.fhce.library.dto.reportes.estudiante;

import java.time.LocalDate;
import java.util.List;

import com.proyecto.fhce.library.dto.reportes.grafico.GraficoDTO;

/**
 * Dashboard del estudiante autenticado. Contiene SOLO datos de ese usuario
 * (el id sale del JWT, nunca del cliente).
 *
 * @param totalPrestamosActivos    préstamos ACTIVO + RENOVADO
 * @param totalPrestamosVencidos   préstamos VENCIDO
 * @param totalPrestamosHistoricos todos los préstamos del usuario (cualquier
 *                                 estado)
 * @param totalReservasActivas     reservas ACTIVA + NOTIFICADA
 * @param totalSancionesActivas    sanciones en estado ACTIVA
 * @param prestamosPorMes          últimos 6 meses (mes actual incluido), meses
 *                                 sin
 *                                 actividad en 0; mismo contrato GraficoDTO que
 *                                 el dashboard del personal
 */
public record EstudianteDashboardDTO(
    Long totalPrestamosActivos,
    Long totalPrestamosVencidos,
    Long totalPrestamosHistoricos,
    Long totalReservasActivas,
    Long totalSancionesActivas,
    CertificadoEstado certificadoNoDeuda,
    List<PrestamoItem> prestamosActivos,
    List<ReservaItem> reservasActivas,
    List<SancionItem> sancionesActivas,
    GraficoDTO prestamosPorMes) {

  /**
   * @param motivo null si está habilitado; si no, texto listo para mostrar al
   *               estudiante
   */
  public record CertificadoEstado(boolean habilitado, String motivo) {
  }

  /**
   * @param tipo          SALA | DOMICILIO
   * @param estado        ACTIVO | RENOVADO | VENCIDO
   * @param diasRestantes calculado en el backend; NEGATIVO = días de retraso
   */
  public record PrestamoItem(
      Long idPrestamo,
      Long idLibro,
      String titulo,
      String tipo,
      String estado,
      LocalDate fechaPrestamo,
      LocalDate fechaEstimadaDevolucion,
      int diasRestantes,
      Long idBiblioteca,
      String nombreBiblioteca) {
  }

  /**
   * @param estado ACTIVA | NOTIFICADA (NOTIFICADA = ya hay ejemplar apartado para
   *               retirar)
   */
  public record ReservaItem(
      Long idReserva,
      Long idLibro,
      String titulo,
      String estado,
      LocalDate fechaReserva,
      Long idBiblioteca,
      String nombreBiblioteca) {
  }

  /**
   * @param motivo      nombre del enum MotivoSancion (p. ej. RETRASO_DEVOLUCION)
   * @param fechaInicio inicio de la suspensión; si es solo multa, fecha en que se
   *                    generó
   * @param fechaFin    fin de la suspensión; null si no tiene (multa sin
   *                    suspensión)
   */
  public record SancionItem(
      Long idSancion,
      String motivo,
      LocalDate fechaInicio,
      LocalDate fechaFin,
      Long idPrestamo,
      Long idBiblioteca,
      String nombreBiblioteca,
      String tituloLibro) {
  }
}