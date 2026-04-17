package com.proyecto.fhce.library.services.loads;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoCertificado;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
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

  @Autowired
  private BibliotecaRepository bibliotecaRepository;

  // @Autowired
  // private NotificacionService notificacionService;
  // @Autowired
  // private AuditoriaService auditoriaService;
  public CertificadoResponse generar(CertificadoRequest request, Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) {

    boolean esAdmin = tieneRol(authorities, "ROLE_ADMIN");
    boolean esBibliotecario = tieneRol(authorities, "ROLE_BIBLIOTECARIO");
    boolean esEstudiante = tieneRol(authorities, "ROLE_ESTUDIANTE");

    Biblioteca biblioteca = bibliotecaRepository.findById(request.getBibliotecaId())
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

    // BIBLIOTECARIO: solo puede operar sobre su biblioteca asignada
    if (esBibliotecario && !esAdmin) {
      boolean esEncargado = biblioteca.getEncargados().stream()
          .anyMatch(enc -> enc.getUsuario().getId_usuario().equals(solicitanteId));

      if (!esEncargado) {
        throw new BusinessException("Solo puede generar certificados de la biblioteca a su cargo");
      }
    }

    // ESTUDIANTE: solo puede generar el suyo propio
    if (esEstudiante && !esBibliotecario && !esAdmin) {
      if (!request.getUsuarioId().equals(solicitanteId)) {
        throw new BusinessException("Solo puede generar su propio certificado");
      }
    }

    Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    Usuario solicitante = usuarioRepository.findById(solicitanteId)
        .orElseThrow(() -> new ResourceNotFoundException("Solicitante no encontrado"));

    validarDeudas(usuario.getId_usuario(), biblioteca.getIdBiblioteca());

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
    certificado.setBibliotecario(solicitante);
    certificado.setBiblioteca(biblioteca);
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
    return "/certificados/" + certificado.getIdCertificado() + ".pdf";
  }

  @Transactional(readOnly = true)
  public List<CertificadoResponse> findByUsuario(Long usuarioId, Long solicitanteId,
      Long bibliotecaId, Collection<? extends GrantedAuthority> authorities) {

    boolean esAdmin = tieneRol(authorities, "ROLE_ADMIN");
    boolean esBibliotecario = tieneRol(authorities, "ROLE_BIBLIOTECARIO");

    // Estudiante solo ve los suyos
    if (!esAdmin && !esBibliotecario && !usuarioId.equals(solicitanteId)) {
      throw new BusinessException("No autorizado para ver estos certificados");
    }

    List<CertificadoNoDeuda> certificados = (bibliotecaId != null)
        ? certificadoRepository.findByUsuario_IdUsuarioAndBiblioteca_IdBiblioteca(usuarioId, bibliotecaId)
        : certificadoRepository.findByUsuario_IdUsuario(usuarioId);

    return certificados.stream().map(this::mapToResponse).toList();
  }

  @Transactional(readOnly = true)
  public CertificadoResponse findById(Long id) {
    CertificadoNoDeuda certificado = certificadoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("certificado no encontrado con id: " + id));
    return mapToResponse(certificado);
  }

  @Transactional(readOnly = true)
  public List<CertificadoResponse> findByBiblioteca(Long bibliotecaId, EstadoCertificado estado,
      Long solicitanteId, Collection<? extends GrantedAuthority> authorities) {

    boolean esAdmin = tieneRol(authorities, "ROLE_ADMIN");

    if (!esAdmin) {
      // Verificar que el bibliotecario sea el encargado de esa biblioteca
      Biblioteca biblioteca = bibliotecaRepository.findById(bibliotecaId)
          .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));
      boolean esEncargado = biblioteca.getEncargados()
          .stream()
          .anyMatch(enc -> enc.getUsuario().getId_usuario().equals(solicitanteId));

      if (!esEncargado) {
        throw new BusinessException("No autorizado para ver certificados de esta biblioteca");
      }
    }

    List<CertificadoNoDeuda> certificados = certificadoRepository
        .findByBibliotecaConFiltroEstado(bibliotecaId, estado);

    return certificados.stream().map(this::mapToResponse).toList();
  }

  @Transactional(readOnly = true)
  public CertificadoResponse findByIdConAutorizacion(
      Long id,
      Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) {

    boolean esAdmin = tieneRol(authorities, "ROLE_ADMIN");
    boolean esBibliotecario = tieneRol(authorities, "ROLE_BIBLIOTECARIO");

    CertificadoNoDeuda certificado = certificadoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Certificado no encontrado con id: " + id));

    Long usuarioId = certificado.getUsuario().getId_usuario();

    // 🔒 Reglas de acceso
    if (!esAdmin && !esBibliotecario && !usuarioId.equals(solicitanteId)) {
      throw new BusinessException("No autorizado para descargar este certificado");
    }

    // Si quieres más control para bibliotecarios (opcional):
    if (esBibliotecario && !esAdmin) {
      boolean perteneceBiblioteca = certificado.getBiblioteca()
          .getEncargados()
          .stream()
          .anyMatch(enc -> enc.getUsuario()
              .getId_usuario()
              .equals(solicitanteId));

      if (!perteneceBiblioteca) {
        throw new BusinessException("No autorizado para esta biblioteca");
      }
    }

    return mapToResponse(certificado);
  }

  public CertificadoResponse anular(Long id, Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) {

    CertificadoNoDeuda certificado = certificadoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Certificado no encontrado: " + id));

    boolean esAdmin = tieneRol(authorities, "ROLE_ADMIN");

    if (!esAdmin) {
      boolean esEncargado = certificado.getBiblioteca().getEncargados()
          .stream()
          .anyMatch(enc -> enc.getUsuario().getId_usuario().equals(solicitanteId));
      if (!esEncargado) {
        throw new BusinessException("No autorizado para anular este certificado");
      }
    }

    if (certificado.getEstadoCertificado() != EstadoCertificado.VIGENTE) {
      throw new BusinessException("Solo se pueden anular certificados en estado VIGENTE");
    }

    certificado.setEstadoCertificado(EstadoCertificado.ANULADO);
    return mapToResponse(certificadoRepository.save(certificado));
  }

  private void validarDeudas(Long usuarioId, Long bibliotecaId) {
    Long activos = prestamoRepository
        .countPrestamosActivosByUsuarioAndBiblioteca(usuarioId, bibliotecaId);
    if (activos > 0)
      throw new BusinessException(
          "El usuario tiene " + activos + " préstamo(s) activo(s) en esta biblioteca");

    Long vencidos = prestamoRepository
        .countPrestamosConEstadoByUsuarioAndBiblioteca(usuarioId, bibliotecaId, EstadoPrestamo.VENCIDO);
    if (vencidos > 0)
      throw new BusinessException(
          "El usuario tiene " + vencidos + " préstamo(s) vencido(s) en esta biblioteca");

    Long renovados = prestamoRepository
        .countPrestamosConEstadoByUsuarioAndBiblioteca(usuarioId, bibliotecaId, EstadoPrestamo.RENOVADO);
    if (renovados > 0)
      throw new BusinessException(
          "El usuario tiene " + renovados + " préstamo(s) con renovación pendiente en esta biblioteca");
  }

  private boolean tieneRol(Collection<? extends GrantedAuthority> authorities, String rol) {
    return authorities.stream().anyMatch(a -> a.getAuthority().equals(rol));
  }

  private CertificadoResponse mapToResponse(CertificadoNoDeuda certificado) {
    CertificadoResponse response = new CertificadoResponse();
    response.setId_certificado(certificado.getIdCertificado());
    response.setUsuario(mapToUsuarioSimple(certificado.getUsuario()));
    if (certificado.getBibliotecario() != null) {
      response.setBibliotecario(mapToUsuarioSimple(certificado.getBibliotecario()));
    }
    response.setFechaEmision(certificado.getFechaEmision());
    response.setFechaVencimiento(certificado.getFechaVencimiento());
    response.setCodigo_verificacion(certificado.getCodigo_verificacion());
    response.setEstadoCertificado(certificado.getEstadoCertificado());
    response.setPdf_generado(certificado.getPdf_generado());
    response.setBibliotecaId(certificado.getBiblioteca().getIdBiblioteca());
    response.setBibliotecaNombre(certificado.getBiblioteca().getNombre());
    response.setUrlDescarga("/api/certificados/" + certificado.getIdCertificado() + "/download");
    return response;
  }

  private UsuarioSimpleResponse mapToUsuarioSimple(Usuario usuario) {
    if (usuario == null)
      return null;

    UsuarioSimpleResponse res = new UsuarioSimpleResponse();

    res.setId_usuario(usuario.getId_usuario());
    res.setUsername(usuario.getUsername());

    if (usuario.getPersona() != null) {
      res.setNombreCompleto(
          usuario.getPersona().getNombre() + " " +
              usuario.getPersona().getApellido_pat() + " " +
              usuario.getPersona().getApellido_mat());

      res.setEmail(usuario.getPersona().getEmail());
      res.setCi(usuario.getPersona().getCi());
    }

    return res;
  }
}