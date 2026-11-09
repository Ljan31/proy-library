package com.proyecto.fhce.library.services.reporte;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.reportes.certificado.*;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.enums.EstadoCertificado;
import com.proyecto.fhce.library.repositories.CertificadoNoDeudaRepository;

@Service
@Transactional(readOnly = true)
public class ReporteCertificadoService {

  private final CertificadoNoDeudaRepository certificadoRepository;

  public ReporteCertificadoService(CertificadoNoDeudaRepository certificadoRepository) {
    this.certificadoRepository = certificadoRepository;
  }

  public CertificadoReporteResponseDTO generarReporteCertificados(
      CertificadoFiltroDTO filtro) {

    ReporteUtil.validarRango(filtro.getFechaInicio(), filtro.getFechaFin());
    LocalDateTime fechaInicio = ReporteUtil.desde(filtro.getFechaInicio());
    LocalDateTime fechaFin = ReporteUtil.hastaExclusivo(filtro.getFechaFin());

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
    // La solicitud puede ser de una persona NO registrada en el sistema
    // (usuario == null): en ese caso se usan los datos que dejó en la solicitud.
    SolicitudCertificado solicitud = c.getSolicitud();
    if (solicitud.getUsuario() != null && solicitud.getUsuario().getPersona() != null) {
      dto.setUsuario(ReporteUtil.nombreCorto(solicitud.getUsuario().getPersona()));
      dto.setCi(solicitud.getUsuario().getPersona().getCi());
    } else {
      dto.setUsuario((solicitud.getNombres() + " " + solicitud.getApellidos()).trim());
      dto.setCi(solicitud.getCi());
    }
    dto.setBiblioteca(c.getBiblioteca().getNombre());
    if (c.getBibliotecario() != null) {
      dto.setBibliotecario(ReporteUtil.nombreCorto(c.getBibliotecario().getPersona()));
    }
    dto.setFechaEmision(c.getFechaEmision());
    dto.setFechaVencimiento(c.getFechaVencimiento());
    dto.setEstadoCertificado(c.getEstadoCertificado().name());
    return dto;
  }
}