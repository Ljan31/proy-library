package com.proyecto.fhce.library.services.reporte;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.prestamo.HistorialPrestamoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.HistorialPrestamoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoReporteDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoReporteResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.ResumenLibrosPrestadosDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.ResumenPrestamoDTO;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.repositories.PrestamoRepository;

@Service
@Transactional(readOnly = true)
public class ReportePrestamoServiceImpl implements ReportePrestamoService {
  @Autowired
  private PrestamoRepository prestamoRepository;

  @Override
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

  @Override
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

  @Override
  public LibroMasPrestadoDTO librosMenosPrestados(LibroMasPrestadoDTO filtro) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'librosMenosPrestados'");
  }

  @Override
  public HistorialPrestamoDTO historialPrestamos(HistorialPrestamoDTO filtro) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'historialPrestamos'");
  }
}
