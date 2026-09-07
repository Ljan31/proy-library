package com.proyecto.fhce.library.services.reporte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
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
  @Autowired
  private PrestamoRepository prestamoRepository;

  public PrestamoReporteResponseDTO generarReportePrestamos(
      PrestamoFiltroDTO filtro) {
    LocalDateTime fechaInicio = filtro.getFechaInicio() != null
        ? filtro.getFechaInicio().atStartOfDay()
        : LocalDateTime.of(1900, 1, 1, 0, 0);

    LocalDateTime fechaFin = filtro.getFechaFin() != null
        ? filtro.getFechaFin().atTime(23, 59, 59)
        : LocalDateTime.of(2999, 12, 31, 23, 59, 59);
    List<Prestamo> prestamos = prestamoRepository.obtenerReportePrestamos(
        filtro.getBibliotecaId(),
        filtro.getEstado(),
        fechaInicio,
        fechaFin);

    List<PrestamoReporteDTO> detalle = prestamos.stream()
        .map(this::mapPrestamoReporte)
        .toList();

    ResumenPrestamoDTO resumen = new ResumenPrestamoDTO();

    resumen.setTotalPrestamos(
        prestamoRepository.countPrestamos(
            filtro.getBibliotecaId()));

    resumen.setActivos(
        prestamoRepository.countByEstado(
            EstadoPrestamo.ACTIVO,
            filtro.getBibliotecaId()));

    resumen.setVencidos(
        prestamoRepository.countByEstado(
            EstadoPrestamo.VENCIDO,
            filtro.getBibliotecaId()));

    resumen.setDevueltos(
        prestamoRepository.countByEstado(
            EstadoPrestamo.DEVUELTO,
            filtro.getBibliotecaId()));

    resumen.setRenovados(
        prestamoRepository.countByEstado(
            EstadoPrestamo.RENOVADO,
            filtro.getBibliotecaId()));

    PrestamoReporteResponseDTO response = new PrestamoReporteResponseDTO();

    response.setResumen(resumen);
    response.setPrestamos(detalle);

    return response;
  }

  private PrestamoReporteDTO mapPrestamoReporte(
      Prestamo p) {

    PrestamoReporteDTO dto = new PrestamoReporteDTO();

    dto.setIdPrestamo(
        p.getIdPrestamo());

    dto.setUsuario(
        p.getUsuario()
            .getPersona()
            .getNombre() + " " +
            p.getUsuario()
                .getPersona()
                .getApellido_pat());

    dto.setCi(
        p.getUsuario()
            .getPersona()
            .getCi());

    dto.setLibro(
        p.getEjemplar()
            .getEdicion()
            .getLibro()
            .getTitulo());

    dto.setIsbn(
        p.getEjemplar()
            .getEdicion()
            .getIsbn());

    dto.setBiblioteca(
        p.getBiblioteca()
            .getNombre());

    dto.setFechaPrestamo(
        p.getFechaPrestamo());

    dto.setFechaDevolucionEstimada(
        p.getFechaDevolucionEstimada());

    dto.setFechaDevolucionReal(
        p.getFechaDevolucionReal());

    dto.setEstadoPrestamo(
        p.getEstadoPrestamo().name());

    dto.setTipoPrestamo(
        p.getTipoPrestamo().name());

    if (p.getEstadoPrestamo() == EstadoPrestamo.VENCIDO) {

      dto.setDiasRetraso(
          (int) java.time.temporal.ChronoUnit.DAYS.between(
              p.getFechaDevolucionEstimada(),
              LocalDate.now()));
    } else {
      dto.setDiasRetraso(0);
    }

    return dto;
  }

  public LibroMasPrestadoResponseDTO obtenerLibrosMasPrestados(
      Long bibliotecaId) {

    List<LibroMasPrestadoDTO> libros = prestamoRepository.obtenerLibrosMasPrestados(
        bibliotecaId);

    Long totalPrestamos = libros.stream()
        .mapToLong(LibroMasPrestadoDTO::getCantidadPrestamos)
        .sum();

    ResumenLibrosPrestadosDTO resumen = new ResumenLibrosPrestadosDTO();

    resumen.setTotalLibros(
        (long) libros.size());

    resumen.setTotalPrestamos(
        totalPrestamos);

    LibroMasPrestadoResponseDTO response = new LibroMasPrestadoResponseDTO();

    response.setResumen(resumen);
    response.setLibros(libros);

    return response;
  }

  public LibroMasPrestadoResponseDTO librosMenosPrestados(Long bibliotecaId) {
    List<LibroMasPrestadoDTO> libros = prestamoRepository.obtenerLibrosMenosPrestados(bibliotecaId);
    Long total = libros.stream().mapToLong(LibroMasPrestadoDTO::getCantidadPrestamos).sum();
    ResumenLibrosPrestadosDTO resumen = new ResumenLibrosPrestadosDTO();
    resumen.setTotalLibros((long) libros.size());
    resumen.setTotalPrestamos(total);
    LibroMasPrestadoResponseDTO response = new LibroMasPrestadoResponseDTO();
    response.setResumen(resumen);
    response.setLibros(libros);
    return response;
  }

  public HistorialPrestamoResponseDTO historialPrestamos(HistorialPrestamoFiltroDTO filtro) {
    LocalDateTime fechaInicio = filtro.getFechaInicio() != null
        ? filtro.getFechaInicio().atStartOfDay()
        : null;
    LocalDateTime fechaFin = filtro.getFechaFin() != null
        ? filtro.getFechaFin().atTime(23, 59, 59)
        : null;

    List<Prestamo> prestamos = prestamoRepository.obtenerHistorialPrestamos(
        filtro.getBibliotecaId(),
        filtro.getUsuarioId(),
        filtro.getLibroId(),
        filtro.getEstado(),
        filtro.getTipoPrestamo(),
        fechaInicio,
        fechaFin);

    List<HistorialPrestamoDTO> detalle = prestamos.stream()
        .map(this::mapHistorial)
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

  // ==================== REPORTE 1 — Préstamos activos del día
  // ====================

  public PrestamosActivosResponseDTO reportePrestamosActivos(Long bibliotecaId) {
    List<Prestamo> prestamos = prestamoRepository
        .findPrestamosActivosParaReporte(bibliotecaId);

    LocalDate hoy = LocalDate.now();
    List<PrestamoActivoDTO> detalle = prestamos.stream()
        .map(p -> mapPrestamoActivo(p, hoy))
        .toList();

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

    PrestamosActivosResponseDTO response = new PrestamosActivosResponseDTO();
    response.setResumen(resumen);
    response.setPrestamos(detalle);
    return response;
  }

  // ==================== REPORTE 2 — Devoluciones pendientes vencidas
  // ====================

  public PrestamosActivosResponseDTO reporteDevolucionesPendientes(Long bibliotecaId) {
    List<Prestamo> prestamos = prestamoRepository
        .findDevolucionesPendientesVencidas(bibliotecaId);

    LocalDate hoy = LocalDate.now();
    List<PrestamoActivoDTO> detalle = prestamos.stream()
        .map(p -> mapPrestamoActivo(p, hoy))
        .toList();

    ResumenPrestamosActivosDTO resumen = new ResumenPrestamosActivosDTO();
    resumen.setFechaReporte(hoy);
    resumen.setTotalRegistros((long) detalle.size());
    // En vencidos, todos tienen vencido=true y diasRetraso > 0
    resumen.setYaVencidos((long) detalle.size());
    resumen.setSoloActivos(0L);
    resumen.setRenovados(0L);
    resumen.setVencenHoy(0L);

    PrestamosActivosResponseDTO response = new PrestamosActivosResponseDTO();
    response.setResumen(resumen);
    response.setPrestamos(detalle);
    return response;
  }

  // ==================== REPORTE 3 — Por vencer en N días ====================

  public PrestamosActivosResponseDTO reportePrestamosPorVencer(
      Long bibliotecaId, int dias) {

    LocalDate hoy = LocalDate.now();
    LocalDate fechaLimite = hoy.plusDays(dias);

    List<Prestamo> prestamos = prestamoRepository
        .findPrestamosPorVencerParaReporte(bibliotecaId, fechaLimite);

    List<PrestamoActivoDTO> detalle = prestamos.stream()
        .map(p -> mapPrestamoActivo(p, hoy))
        .toList();

    ResumenPrestamosActivosDTO resumen = new ResumenPrestamosActivosDTO();
    resumen.setFechaReporte(hoy);
    resumen.setDiasVentana(dias);
    resumen.setTotalRegistros((long) detalle.size());
    resumen.setSoloActivos(detalle.stream()
        .filter(d -> "ACTIVO".equals(d.getEstadoPrestamo())).count());
    resumen.setRenovados(detalle.stream()
        .filter(d -> "RENOVADO".equals(d.getEstadoPrestamo())).count());
    resumen.setVencenHoy(detalle.stream()
        .filter(d -> hoy.equals(d.getFechaDevolucionEstimada())).count());
    resumen.setYaVencidos(0L); // la query excluye vencidos por diseño

    PrestamosActivosResponseDTO response = new PrestamosActivosResponseDTO();
    response.setResumen(resumen);
    response.setPrestamos(detalle);
    return response;
  }

  // ==================== Mapper privado compartido ====================

  private PrestamoActivoDTO mapPrestamoActivo(Prestamo p, LocalDate hoy) {
    PrestamoActivoDTO dto = new PrestamoActivoDTO();

    dto.setIdPrestamo(p.getIdPrestamo());
    dto.setEstadoPrestamo(p.getEstadoPrestamo().name());
    dto.setTipoPrestamo(p.getTipoPrestamo().name());
    dto.setFechaPrestamo(p.getFechaPrestamo());
    dto.setFechaDevolucionEstimada(p.getFechaDevolucionEstimada());
    dto.setRenovaciones(p.getRenovaciones());

    // Cálculo de vencimiento en Java — nunca hardcodeado en la query
    long diasDiferencia = ChronoUnit.DAYS.between(hoy, p.getFechaDevolucionEstimada());
    // diasDiferencia < 0 → ya venció (días de retraso = valor absoluto)
    // diasDiferencia == 0 → vence hoy
    // diasDiferencia > 0 → días que faltan
    dto.setDiasRestantes((int) diasDiferencia);
    boolean vencido = diasDiferencia < 0;
    dto.setVencido(vencido);
    dto.setDiasRetraso(vencido ? (int) Math.abs(diasDiferencia) : 0);

    // Usuario
    dto.setIdUsuario(p.getUsuario().getId_usuario());
    dto.setNombreUsuario(
        p.getUsuario().getPersona().getNombre() + " " +
            p.getUsuario().getPersona().getApellido_pat());
    dto.setCi(p.getUsuario().getPersona().getCi());

    // Ejemplar y libro
    dto.setIdEjemplar(p.getEjemplar().getIdEjemplar());
    dto.setCodigoEjemplar(p.getEjemplar().getCodigoEjemplar());
    dto.setTituloLibro(p.getEjemplar().getEdicion().getLibro().getTitulo());
    dto.setIsbn(p.getEjemplar().getEdicion().getIsbn());
    dto.setEditorial(p.getEjemplar().getEdicion().getEditorial());

    // Biblioteca
    dto.setIdBiblioteca(p.getBiblioteca().getIdBiblioteca());
    dto.setNombreBiblioteca(p.getBiblioteca().getNombre());

    return dto;
  }

  private HistorialPrestamoDTO mapHistorial(Prestamo p) {
    HistorialPrestamoDTO dto = new HistorialPrestamoDTO();
    dto.setIdPrestamo(p.getIdPrestamo());
    dto.setUsuario(p.getUsuario().getPersona().getNombre() + " "
        + p.getUsuario().getPersona().getApellido_pat());
    dto.setCi(p.getUsuario().getPersona().getCi());
    dto.setLibro(p.getEjemplar().getEdicion().getLibro().getTitulo());
    dto.setIsbn(p.getEjemplar().getEdicion().getIsbn());
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
      // deterioro si el ordinal de devolución es MAYOR que el de entrega
      // (EXCELENTE=0, BUENO=1, REGULAR=2, MALO=3, DETERIORADO=4)
      boolean deterioro = p.getCondicionDevolucion().ordinal() > p.getCondicionEntrega().ordinal();
      dto.setEntregadoConDeterioro(deterioro);
    }
    // días de retraso: aplica a VENCIDO y DEVUELTO tardío
    if (p.getEstadoPrestamo() == EstadoPrestamo.VENCIDO) {
      dto.setDiasRetraso((int) ChronoUnit.DAYS.between(
          p.getFechaDevolucionEstimada(), LocalDate.now()));
    } else if (p.getEstadoPrestamo() == EstadoPrestamo.DEVUELTO
        && p.getFechaDevolucionReal() != null
        && p.getFechaDevolucionReal().toLocalDate()
            .isAfter(p.getFechaDevolucionEstimada())) {
      dto.setDiasRetraso((int) ChronoUnit.DAYS.between(
          p.getFechaDevolucionEstimada(),
          p.getFechaDevolucionReal().toLocalDate()));
    } else {
      dto.setDiasRetraso(0);
    }
    return dto;
  }
}
