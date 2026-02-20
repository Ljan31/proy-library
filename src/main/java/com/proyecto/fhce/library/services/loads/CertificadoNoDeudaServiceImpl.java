package com.proyecto.fhce.library.services.loads;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoCertificado;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.CertificadoNoDeudaRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
@Transactional
public class CertificadoNoDeudaServiceImpl implements CertificadoNoDeudaService {
  @Autowired
  private CertificadoNoDeudaRepository certificadoRepository;
  @Autowired
  private UserRepository usuarioRepository;
  // @Autowired
  // private SancionRepository sancionRepository;
  @Autowired
  private PrestamoRepository prestamoRepository;

  // @Autowired
  // private NotificacionService notificacionService;
  // @Autowired
  // private AuditoriaService auditoriaService;
  public CertificadoResponse generar(CertificadoRequest request, Long bibliotecarioId) {
    Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    Usuario bibliotecario = usuarioRepository.findById(bibliotecarioId)
        .orElseThrow(() -> new ResourceNotFoundException("Bibliotecario no encontrado"));

    // Validar que no tenga préstamos activos
    Long prestamosActivos = prestamoRepository.countPrestamosActivosByUsuario(usuario.getId_usuario());
    if (prestamosActivos > 0) {
      throw new BusinessException("El usuario tiene " + prestamosActivos + " préstamo(s) activo(s)");
    }

    // ** Validar que no tenga sanciones activas
    // if (sancionRepository.hasUsuarioSancionesActivas(usuario.getId_usuario())) {
    // BigDecimal montoDeuda =
    // sancionRepository.sumMontoSancionesActivas(usuario.getId_usuario());
    // throw new BusinessException("El usuario tiene sanciones activas por un monto
    // de Bs. " + montoDeuda);
    // }

    // Generar certificado
    CertificadoNoDeuda certificado = new CertificadoNoDeuda();
    certificado.setUsuario(usuario);
    certificado.setBibliotecario(bibliotecario);
    certificado.setFechaEmision(LocalDateTime.now());

    if (request.getDiasValidez() != null) {
      certificado.setFechaVencimiento(
          LocalDateTime.now().plusDays(request.getDiasValidez()));
    }

    certificado.setEstadoCertificado(EstadoCertificado.VIGENTE);
    // El código de verificación se genera automáticamente en @PrePersist

    CertificadoNoDeuda saved = certificadoRepository.save(certificado);

    // Generar PDF (implementación simplificada)
    String pdfPath = generarPDF(saved);
    saved.setPdf_generado(pdfPath);
    certificadoRepository.save(saved);

    // auditoriaService.registrar("GENERATE_CERTIFICATE", "no_debt_certificates",
    // saved.getId_certificado(), null, "Certificado generado");

    // notificacionService.enviarNotificacionCertificado(saved);

    return mapToResponse(saved);
  }

  @Transactional(readOnly = true)
  public ValidacionCertificadoResponse validar(String codigoVerificacion) {
    ValidacionCertificadoResponse response = new ValidacionCertificadoResponse();
    Optional<CertificadoNoDeuda> certificadoOpt = certificadoRepository.findByCodigoVerificacion(codigoVerificacion);

    if (certificadoOpt.isEmpty()) {
      response.setValido(false);
      response.setMensaje("Código de verificación no encontrado");
      return response;
    }

    CertificadoNoDeuda certificado = certificadoOpt.get();

    if (certificado.getEstadoCertificado() != EstadoCertificado.VIGENTE) {
      response.setValido(false);
      response.setMensaje("El certificado no está vigente");
      response.setCertificado(mapToResponse(certificado));
      return response;
    }

    if (certificado.getFechaVencimiento() != null &&
        LocalDateTime.now().isAfter(certificado.getFechaVencimiento())) {
      response.setValido(false);
      response.setMensaje("El certificado ha vencido");
      response.setCertificado(mapToResponse(certificado));
      return response;
    }

    response.setValido(true);
    response.setMensaje("Certificado válido");
    response.setCertificado(mapToResponse(certificado));

    return response;
  }

  @Scheduled(cron = "0 0 2 * * *") // Todos los días a las 2 AM
  public void actualizarCertificadosVencidos() {
    List<CertificadoNoDeuda> vencidos = certificadoRepository.findCertificadosVencidos(LocalDateTime.now());
    for (CertificadoNoDeuda certificado : vencidos) {
      certificado.setEstadoCertificado(EstadoCertificado.VENCIDO);
      certificadoRepository.save(certificado);
    }
  }

  private String generarPDF(CertificadoNoDeuda certificado) {
    // Aquí implementarías la generación del PDF usando iText, JasperReports, etc.
    // Por ahora retornamos una ruta simulada
    return "/certificados/" + certificado.getId_certificado() + ".pdf";
  }

  private CertificadoResponse mapToResponse(CertificadoNoDeuda certificado) {
    CertificadoResponse response = new CertificadoResponse();
    response.setId_certificado(certificado.getId_certificado());
    response.setFechaEmision(certificado.getFechaEmision());
    response.setFechaVencimiento(certificado.getFechaVencimiento());
    response.setCodigo_verificacion(certificado.getCodigo_verificacion());
    response.setEstadoCertificado(certificado.getEstadoCertificado());
    response.setPdf_generado(certificado.getPdf_generado());
    response.setUrlDescarga("/api/certificados/" + certificado.getId_certificado() + "/download");
    return response;
  }
}