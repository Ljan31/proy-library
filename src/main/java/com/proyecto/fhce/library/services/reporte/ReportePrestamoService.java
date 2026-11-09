package com.proyecto.fhce.library.services.reporte;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.prestamo.HistorialPrestamoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.HistorialPrestamoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.HistorialPrestamoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoActivoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoReporteDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoReporteResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamosActivosResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.ResumenHistorialDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.ResumenLibrosPrestadosDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.ResumenPrestamoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.ResumenPrestamosActivosDTO;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.repositories.PrestamoRepository;

@Service
@Transactional(readOnly = true)
public class ReportePrestamoService {

  private final PrestamoRepository prestamoRepository;
  private final Clock clock;

  public ReportePrestamoService(PrestamoRepository prestamoRepository, Clock clock) {
    this.prestamoRepository = prestamoRepository;
    this.clock = clock;
  }

  // ==================== Reporte general de préstamos ====================

  public PrestamoReporteResponseDTO generarReportePrestamos(PrestamoFiltroDTO filtro) {
    ReporteUtil.validarRango(filtro.getFechaInicio(), filtro.getFechaFin());
    LocalDate hoy = LocalDate.now(clock);

    List<Prestamo> prestamos = prestamoRepository.obtenerReportePrestamos(
        filtro.getBibliotecaId(),
        filtro.getEstado(),
        ReporteUtil.desde(filtro.getFechaInicio()),
        ReporteUtil.hastaExclusivo(filtro.getFechaFin()));

    List<PrestamoReporteDTO> detalle = prestamos.stream()
        .map(p -> mapPrestamoReporte(p, hoy))
        .toList();

    // El resumen se calcula sobre el MISMO conjunto filtrado que el detalle,
    // para que los totales siempre cuadren con la tabla.
    ResumenPrestamoDTO resumen = new ResumenPrestamoDTO();
    resumen.setTotalPrestamos((long) detalle.size());
    resumen.setActivos(contarEstado(detalle, EstadoPrestamo.ACTIVO));
    resumen.setVencidos(contarEstado(detalle, EstadoPrestamo.VENCIDO));
    resumen.setDevueltos(contarEstado(detalle, EstadoPrestamo.DEVUELTO));
    resumen.setRenovados(contarEstado(detalle, EstadoPrestamo.RENOVADO));

    PrestamoReporteResponseDTO response = new PrestamoReporteResponseDTO();
    response.setResumen(resumen);
    response.setPrestamos(detalle);
    return response;
  }

  private long contarEstado(List<PrestamoReporteDTO> detalle, EstadoPrestamo estado) {
    return detalle.stream()
        .filter(d -> estado.name().equals(d.getEstadoPrestamo()))
        .count();
  }

  private PrestamoReporteDTO mapPrestamoReporte(Prestamo p, LocalDate hoy) {
    PrestamoReporteDTO dto = new PrestamoReporteDTO();
    dto.setIdPrestamo(p.getIdPrestamo());
    dto.setUsuario(ReporteUtil.nombreCorto(p.getUsuario().getPersona()));
    dto.setCi(p.getUsuario().getPersona().getCi());
    dto.setLibro(p.getEjemplar().getEdicion().getLibro().getTitulo());
    dto.setBiblioteca(p.getBiblioteca().getNombre());
    dto.setFechaPrestamo(p.getFechaPrestamo());
    dto.setFechaDevolucionEstimada(p.getFechaDevolucionEstimada());
    dto.setFechaDevolucionReal(p.getFechaDevolucionReal());
    dto.setEstadoPrestamo(p.getEstadoPrestamo().name());
    dto.setTipoPrestamo(p.getTipoPrestamo().name());
    dto.setDiasRetraso(ReporteUtil.diasRetraso(p, hoy));
    return dto;
  }

  // ==================== Libros más / menos prestados ====================

  public LibroMasPrestadoResponseDTO obtenerLibrosMasPrestados(Long bibliotecaId) {
    return armarRespuestaLibros(prestamoRepository.obtenerLibrosMasPrestados(bibliotecaId));
  }

  public LibroMasPrestadoResponseDTO librosMenosPrestados(Long bibliotecaId) {
    return armarRespuestaLibros(prestamoRepository.obtenerLibrosMenosPrestados(bibliotecaId));
  }

  private LibroMasPrestadoResponseDTO armarRespuestaLibros(List<LibroMasPrestadoDTO> libros) {
    long totalPrestamos = libros.stream()
        .mapToLong(LibroMasPrestadoDTO::getCantidadPrestamos)
        .sum();

    ResumenLibrosPrestadosDTO resumen = new ResumenLibrosPrestadosDTO();
    resumen.setTotalLibros((long) libros.size());
    resumen.setTotalPrestamos(totalPrestamos);

    LibroMasPrestadoResponseDTO response = new LibroMasPrestadoResponseDTO();
    response.setResumen(resumen);
    response.setLibros(libros);
    return response;
  }

  // ==================== Historial ====================

  public HistorialPrestamoResponseDTO historialPrestamos(HistorialPrestamoFiltroDTO filtro) {
    ReporteUtil.validarRango(filtro.getFechaInicio(), filtro.getFechaFin());
    LocalDate hoy = LocalDate.now(clock);

    List<Prestamo> prestamos = prestamoRepository.obtenerHistorialPrestamos(
        filtro.getBibliotecaId(),
        filtro.getUsuarioId(),
        filtro.getLibroId(),
        filtro.getEstado(),
        filtro.getTipoPrestamo(),
        ReporteUtil.desde(filtro.getFechaInicio()),
        ReporteUtil.hastaExclusivo(filtro.getFechaFin()));

    List<HistorialPrestamoDTO> detalle = prestamos.stream()
        .map(p -> mapHistorial(p, hoy))
        .toList();

    ResumenHistorialDTO resumen = new ResumenHistorialDTO();
    resumen.setTotalPrestamos((long) detalle.size());
    resumen.setTotalDevueltos(detalle.stream()
        .filter(d -> "DEVUELTO".equals(d.getEstadoPrestamo())).count());
    resumen.setTotalVencidos(detalle.stream()
        .filter(d -> "VENCIDO".equals(d.getEstadoPrestamo())).count());
    resumen.setTotalRenovados(detalle.stream()
        .filter(d -> "RENOVADO".equals(d.getEstadoPrestamo())).count());
    resumen.setTotalConDeterioro(detalle.stream()
        .filter(HistorialPrestamoDTO::isEntregadoConDeterioro).count());
    resumen.setTotalConRetraso(detalle.stream()
        .filter(d -> d.getDiasRetraso() > 0).count());

    HistorialPrestamoResponseDTO response = new HistorialPrestamoResponseDTO();
    response.setResumen(resumen);
    response.setPrestamos(detalle);
    return response;
  }

  private HistorialPrestamoDTO mapHistorial(Prestamo p, LocalDate hoy) {
    HistorialPrestamoDTO dto = new HistorialPrestamoDTO();
    dto.setIdPrestamo(p.getIdPrestamo());
    dto.setUsuario(ReporteUtil.nombreCorto(p.getUsuario().getPersona()));
    dto.setCi(p.getUsuario().getPersona().getCi());
    dto.setLibro(p.getEjemplar().getEdicion().getLibro().getTitulo());
    dto.setBiblioteca(p.getBiblioteca().getNombre());
    dto.setTipoPrestamo(p.getTipoPrestamo().name());
    dto.setEstadoPrestamo(p.getEstadoPrestamo().name());
    dto.setFechaPrestamo(p.getFechaPrestamo());
    dto.setFechaDevolucionEstimada(p.getFechaDevolucionEstimada());
    dto.setFechaDevolucionReal(p.getFechaDevolucionReal());
    dto.setRenovaciones(p.getRenovaciones());

    if (p.getCondicionEntrega() != null) {
      dto.setCondicionEntrega(p.getCondicionEntrega().name());
    }
    if (p.getCondicionDevolucion() != null) {
      dto.setCondicionDevolucion(p.getCondicionDevolucion().name());
    }
    // null-safe: antes lanzaba NPE si condicionEntrega era null
    Integer diferencia = ReporteUtil.diferenciaCondicion(
        p.getCondicionEntrega(), p.getCondicionDevolucion());
    dto.setEntregadoConDeterioro(diferencia != null && diferencia > 0);

    dto.setDiasRetraso(ReporteUtil.diasRetraso(p, hoy));
    return dto;
  }

  // ==================== Reportes operativos (activos / vencidos / por vencer)
  // ====================

  public PrestamosActivosResponseDTO reportePrestamosActivos(Long bibliotecaId) {
    LocalDate hoy = LocalDate.now(clock);
    List<PrestamoActivoDTO> detalle = prestamoRepository
        .findPrestamosActivosParaReporte(bibliotecaId).stream()
        .map(p -> mapPrestamoActivo(p, hoy))
        .toList();

    ResumenPrestamosActivosDTO resumen = resumenOperativo(detalle, hoy);
    return respuestaOperativa(resumen, detalle);
  }

  public PrestamosActivosResponseDTO reporteDevolucionesPendientes(Long bibliotecaId) {
    LocalDate hoy = LocalDate.now(clock);
    List<PrestamoActivoDTO> detalle = prestamoRepository
        .findDevolucionesPendientesVencidas(bibliotecaId).stream()
        .map(p -> mapPrestamoActivo(p, hoy))
        .toList();

    ResumenPrestamosActivosDTO resumen = resumenOperativo(detalle, hoy);
    return respuestaOperativa(resumen, detalle);
  }

  public PrestamosActivosResponseDTO reportePrestamosPorVencer(Long bibliotecaId, int dias) {
    LocalDate hoy = LocalDate.now(clock);
    LocalDate fechaLimite = hoy.plusDays(dias);

    List<PrestamoActivoDTO> detalle = prestamoRepository
        .findPrestamosPorVencerParaReporte(bibliotecaId, hoy, fechaLimite).stream()
        .map(p -> mapPrestamoActivo(p, hoy))
        .toList();

    ResumenPrestamosActivosDTO resumen = resumenOperativo(detalle, hoy);
    resumen.setDiasVentana(dias);
    return respuestaOperativa(resumen, detalle);
  }

  /**
   * Un único cálculo de resumen para los tres reportes operativos.
   * Antes cada reporte lo armaba a mano (y con valores fijos en 0).
   */
  private ResumenPrestamosActivosDTO resumenOperativo(List<PrestamoActivoDTO> detalle, LocalDate hoy) {
    ResumenPrestamosActivosDTO resumen = new ResumenPrestamosActivosDTO();
    resumen.setFechaReporte(hoy);
    resumen.setTotalRegistros((long) detalle.size());
    resumen.setSoloActivos(detalle.stream()
        .filter(d -> "ACTIVO".equals(d.getEstadoPrestamo())).count());
    resumen.setRenovados(detalle.stream()
        .filter(d -> "RENOVADO".equals(d.getEstadoPrestamo())).count());
    resumen.setYaVencidos(detalle.stream()
        .filter(PrestamoActivoDTO::isVencido).count());
    resumen.setVencenHoy(detalle.stream()
        .filter(d -> hoy.equals(d.getFechaDevolucionEstimada())).count());
    return resumen;
  }

  private PrestamosActivosResponseDTO respuestaOperativa(
      ResumenPrestamosActivosDTO resumen, List<PrestamoActivoDTO> detalle) {
    PrestamosActivosResponseDTO response = new PrestamosActivosResponseDTO();
    response.setResumen(resumen);
    response.setPrestamos(detalle);
    return response;
  }

  private PrestamoActivoDTO mapPrestamoActivo(Prestamo p, LocalDate hoy) {
    PrestamoActivoDTO dto = new PrestamoActivoDTO();

    dto.setIdPrestamo(p.getIdPrestamo());
    dto.setEstadoPrestamo(p.getEstadoPrestamo().name());
    dto.setTipoPrestamo(p.getTipoPrestamo().name());
    dto.setFechaPrestamo(p.getFechaPrestamo());
    dto.setFechaDevolucionEstimada(p.getFechaDevolucionEstimada());
    dto.setRenovaciones(p.getRenovaciones());

    // < 0 ya venció, == 0 vence hoy, > 0 días que faltan
    long diasDiferencia = ChronoUnit.DAYS.between(hoy, p.getFechaDevolucionEstimada());
    dto.setDiasRestantes((int) diasDiferencia);
    dto.setVencido(diasDiferencia < 0);
    dto.setDiasRetraso(diasDiferencia < 0 ? (int) -diasDiferencia : 0);

    dto.setIdUsuario(p.getUsuario().getId_usuario());
    dto.setNombreUsuario(ReporteUtil.nombreCorto(p.getUsuario().getPersona()));
    dto.setCi(p.getUsuario().getPersona().getCi());

    dto.setIdEjemplar(p.getEjemplar().getIdEjemplar());
    dto.setCodigoEjemplar(p.getEjemplar().getCodigoEjemplar());
    dto.setTituloLibro(p.getEjemplar().getEdicion().getLibro().getTitulo());
    dto.setEditorial(p.getEjemplar().getEdicion().getEditorial());
    // TODO isbn: el frontend lo declara pero Edicion no expone el campo.
    // Ver nota de entrega (opción A: agregar isbn a Edicion + PrestamoActivoDTO;
    // opción B: quitarlo de reporte.service.ts).

    dto.setIdBiblioteca(p.getBiblioteca().getIdBiblioteca());
    dto.setNombreBiblioteca(p.getBiblioteca().getNombre());
    return dto;
  }
}