package com.proyecto.fhce.library.services.reporte;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.certificado.*;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.enums.EstadoCertificado;
import com.proyecto.fhce.library.repositories.CertificadoNoDeudaRepository;

@Service
@Transactional(readOnly = true)
public class ReporteCertificadoService {

  @Autowired
  private CertificadoNoDeudaRepository certificadoRepository;

  public CertificadoReporteResponseDTO generarReporteCertificados(
      CertificadoFiltroDTO filtro) {

    LocalDateTime fechaInicio = filtro.getFechaInicio() != null
        ? filtro.getFechaInicio().atStartOfDay()
        : null;
    LocalDateTime fechaFin = filtro.getFechaFin() != null
        ? filtro.getFechaFin().atTime(23, 59, 59)
        : null;

    List<CertificadoNoDeuda> certificados = certificadoRepository
        .obtenerReporteCertificados(
            filtro.getBibliotecaId(),
            filtro.getBibliotecarioId(),
            filtro.getEstado(),
            fechaInicio,
            fechaFin);

    List<CertificadoReporteItemDTO> detalle = certificados.stream()
        .map(this::mapCertificado)
        .toList();

    CertificadoResumenDTO resumen = new CertificadoResumenDTO();
    resumen.setTotalEmitidos((long) detalle.size());
    resumen.setVigentes(detalle.stream()
        .filter(c -> "VIGENTE".equals(c.getEstadoCertificado())).count());
    resumen.setVencidos(detalle.stream()
        .filter(c -> "VENCIDO".equals(c.getEstadoCertificado())).count());
    resumen.setAnulados(detalle.stream()
        .filter(c -> "ANULADO".equals(c.getEstadoCertificado())).count());

    CertificadoReporteResponseDTO response = new CertificadoReporteResponseDTO();
    response.setResumen(resumen);
    response.setCertificados(detalle);
    return response;
  }

  private CertificadoReporteItemDTO mapCertificado(CertificadoNoDeuda c) {
    CertificadoReporteItemDTO dto = new CertificadoReporteItemDTO();
    dto.setIdCertificado(c.getIdCertificado());
    dto.setCodigoVerificacion(c.getCodigoVerificacion());
    dto.setUsuario(c.getUsuario().getPersona().getNombre() + " "
        + c.getUsuario().getPersona().getApellido_pat());
    dto.setCi(c.getUsuario().getPersona().getCi());
    dto.setBiblioteca(c.getBiblioteca().getNombre());
    if (c.getBibliotecario() != null) {
      dto.setBibliotecario(c.getBibliotecario().getPersona().getNombre() + " "
          + c.getBibliotecario().getPersona().getApellido_pat());
    }
    dto.setFechaEmision(c.getFechaEmision());
    dto.setFechaVencimiento(c.getFechaVencimiento());
    dto.setEstadoCertificado(c.getEstadoCertificado().name());
    return dto;
  }
}