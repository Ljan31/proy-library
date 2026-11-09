package com.proyecto.fhce.library.services.reporte;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.sancion.SancionFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.sancion.SancionReporteItemDTO;
import com.proyecto.fhce.library.dto.reportes.sancion.SancionReporteResponseDTO;
import com.proyecto.fhce.library.dto.reportes.sancion.SancionResumenDTO;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.repositories.SancionRepository;

@Service
@Transactional(readOnly = true)
public class ReporteSancionService {

  private final SancionRepository sancionRepository;

  public ReporteSancionService(SancionRepository sancionRepository) {
    this.sancionRepository = sancionRepository;
  }

  public SancionReporteResponseDTO generarReporteSanciones(SancionFiltroDTO filtro) {
    ReporteUtil.validarRango(filtro.getFechaInicio(), filtro.getFechaFin());

    // Bug anterior: se calculaban fechaInicio/fechaFin como LocalDateTime pero se
    // pasaban los LocalDate originales a la query. Ahora se filtra por
    // fechaGeneracion (LocalDateTime) con rango semiabierto [inicio, fin).
    List<Sancion> sanciones = sancionRepository.obtenerReporteSanciones(
        filtro.getBibliotecaId(),
        filtro.getUsuarioId(),
        filtro.getTipo(),
        filtro.getEstado(),
        ReporteUtil.desde(filtro.getFechaInicio()),
        ReporteUtil.hastaExclusivo(filtro.getFechaFin()));

    List<SancionReporteItemDTO> detalle = sanciones.stream()
        .map(this::mapSancion)
        .toList();

    SancionReporteResponseDTO response = new SancionReporteResponseDTO();
    response.setResumen(buildResumen(sanciones));
    response.setSanciones(detalle);
    return response;
  }

  /**
   * Resumen calculado sobre las mismas sanciones del detalle (respeta los
   * filtros) y en memoria: cero queries extra frente a las 9 anteriores.
   */
  private SancionResumenDTO buildResumen(List<Sancion> sanciones) {
    SancionResumenDTO r = new SancionResumenDTO();
    r.setTotalSanciones((long) sanciones.size());
    r.setActivas(contarEstado(sanciones, EstadoSancion.ACTIVA));
    r.setPagadas(contarEstado(sanciones, EstadoSancion.PAGADA));
    r.setCondonadas(contarEstado(sanciones, EstadoSancion.CONDONADA));
    r.setMultas(contarTipo(sanciones, TipoSancion.MULTA));
    r.setSuspensiones(contarTipo(sanciones, TipoSancion.SUSPENSION));
    r.setMontoTotalGenerado(sumarMonto(sanciones, null));
    r.setMontoTotalPagado(sumarMonto(sanciones, EstadoSancion.PAGADA));
    r.setMontoPendiente(sumarMonto(sanciones, EstadoSancion.ACTIVA));
    return r;
  }

  private long contarEstado(List<Sancion> sanciones, EstadoSancion estado) {
    return sanciones.stream().filter(s -> s.getEstado() == estado).count();
  }

  private long contarTipo(List<Sancion> sanciones, TipoSancion tipo) {
    return sanciones.stream().filter(s -> s.getTipoSancion() == tipo).count();
  }

  private BigDecimal sumarMonto(List<Sancion> sanciones, EstadoSancion estado) {
    return sanciones.stream()
        .filter(s -> estado == null || s.getEstado() == estado)
        .map(Sancion::getMontoMulta)
        .filter(m -> m != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private SancionReporteItemDTO mapSancion(Sancion s) {
    SancionReporteItemDTO dto = new SancionReporteItemDTO();
    dto.setIdSancion(s.getIdSancion());
    dto.setUsuario(ReporteUtil.nombreCorto(s.getUsuario().getPersona()));
    dto.setCi(s.getUsuario().getPersona().getCi());
    // La biblioteca viene de la sanción (NOT NULL), no del préstamo:
    // las sanciones manuales no tienen préstamo y antes provocaban NPE.
    dto.setBiblioteca(s.getBiblioteca().getNombre());
    dto.setTipoSancion(s.getTipoSancion().name());
    dto.setEstadoSancion(s.getEstado().name());
    dto.setMonto(s.getMontoMulta());
    dto.setDiasSuspension(s.getDiasSuspension());
    dto.setDiasRetraso(s.getDiasRetraso());
    dto.setFechaInicio(s.getFechaInicioSuspension());
    dto.setFechaFin(s.getFechaFinSuspension());

    Prestamo prestamo = s.getPrestamo();
    if (prestamo != null) {
      dto.setIdPrestamoOrigen(prestamo.getIdPrestamo());
      dto.setLibroOrigen(prestamo.getEjemplar().getEdicion().getLibro().getTitulo());
    }
    return dto;
  }
}