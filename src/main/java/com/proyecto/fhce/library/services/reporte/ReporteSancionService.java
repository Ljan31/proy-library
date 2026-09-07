package com.proyecto.fhce.library.services.reporte;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.sancion.*;
import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.repositories.SancionRepository;

@Service
@Transactional(readOnly = true)
public class ReporteSancionService {

  @Autowired
  private SancionRepository sancionRepository;

  public SancionReporteResponseDTO generarReporteSanciones(SancionFiltroDTO filtro) {
    LocalDateTime fechaInicio = filtro.getFechaInicio() != null
        ? filtro.getFechaInicio().atStartOfDay()
        : null;
    LocalDateTime fechaFin = filtro.getFechaFin() != null
        ? filtro.getFechaFin().atTime(23, 59, 59)
        : null;

    List<Sancion> sanciones = sancionRepository.obtenerReporteSanciones(
        filtro.getBibliotecaId(),
        filtro.getUsuarioId(),
        filtro.getTipo(),
        filtro.getEstado(),
        filtro.getFechaInicio(),
        filtro.getFechaFin());

    List<SancionReporteItemDTO> detalle = sanciones.stream()
        .map(this::mapSancion)
        .toList();

    SancionResumenDTO resumen = buildResumen(filtro.getBibliotecaId());

    SancionReporteResponseDTO response = new SancionReporteResponseDTO();
    response.setResumen(resumen);
    response.setSanciones(detalle);
    return response;
  }

  private SancionResumenDTO buildResumen(Long bibliotecaId) {
    SancionResumenDTO r = new SancionResumenDTO();
    r.setTotalSanciones(
        sancionRepository.countByTipoAndEstadoAndBiblioteca(null, null, bibliotecaId));
    r.setActivas(
        sancionRepository.countByTipoAndEstadoAndBiblioteca(null, EstadoSancion.ACTIVA, bibliotecaId));
    r.setPagadas(
        sancionRepository.countByTipoAndEstadoAndBiblioteca(null, EstadoSancion.PAGADA, bibliotecaId));
    r.setCondonadas(
        sancionRepository.countByTipoAndEstadoAndBiblioteca(null, EstadoSancion.CONDONADA, bibliotecaId));
    r.setMultas(
        sancionRepository.countByTipoAndEstadoAndBiblioteca(TipoSancion.MULTA, null, bibliotecaId));
    r.setSuspensiones(
        sancionRepository.countByTipoAndEstadoAndBiblioteca(TipoSancion.SUSPENSION, null, bibliotecaId));
    r.setMontoTotalGenerado(
        sancionRepository.sumMontoByEstadoAndBiblioteca(null, bibliotecaId));
    r.setMontoTotalPagado(
        sancionRepository.sumMontoByEstadoAndBiblioteca(EstadoSancion.PAGADA, bibliotecaId));
    // monto pendiente = activas
    r.setMontoPendiente(
        sancionRepository.sumMontoByEstadoAndBiblioteca(EstadoSancion.ACTIVA, bibliotecaId));
    return r;
  }

  private SancionReporteItemDTO mapSancion(Sancion s) {
    SancionReporteItemDTO dto = new SancionReporteItemDTO();
    dto.setIdSancion(s.getIdSancion());
    dto.setUsuario(s.getUsuario().getPersona().getNombre() + " "
        + s.getUsuario().getPersona().getApellido_pat());
    dto.setCi(s.getUsuario().getPersona().getCi());
    dto.setBiblioteca(s.getPrestamo().getBiblioteca().getNombre());
    dto.setTipoSancion(s.getTipoSancion().name());
    dto.setEstadoSancion(s.getEstado().name());
    dto.setMonto(s.getMontoMulta());
    dto.setDiasSuspension(s.getDiasSuspension());
    dto.setDiasRetraso(s.getDiasRetraso());
    dto.setFechaInicio(s.getFechaInicioSuspension());
    dto.setFechaFin(s.getFechaFinSuspension());
    dto.setIdPrestamoOrigen(s.getPrestamo().getIdPrestamo());
    dto.setLibroOrigen(s.getPrestamo().getEjemplar()
        .getEdicion().getLibro().getTitulo());
    return dto;
  }
}