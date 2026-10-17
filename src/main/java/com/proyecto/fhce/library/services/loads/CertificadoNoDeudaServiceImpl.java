package com.proyecto.fhce.library.services.loads;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.library.UserCertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.entities.RazonCertificado;
import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoCertificado;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.EstadoSolicitud;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.CertificadoNoDeudaRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.RazonCertificadoRepository;
import com.proyecto.fhce.library.repositories.SolicitudCertificadoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.DocxToPdfConverter;
import com.proyecto.fhce.library.services.library.CertificadoDocxService;
import com.proyecto.fhce.library.services.notificaciones.NotificacionService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class CertificadoNoDeudaServiceImpl implements CertificadoNoDeudaService {

  private final CertificadoNoDeudaRepository certificadoRepository;
  private final UserRepository usuarioRepository;
  // // final private SancionRepository sancionRepository;
  private final PrestamoRepository prestamoRepository;
  private final BibliotecaRepository bibliotecaRepository;
  private final NotificacionService notificacionService;
  private final CertificadoDocxService certificadoDocxService;
  private final DocxToPdfConverter pdfConverter;
  private final SolicitudCertificadoRepository solicitudCertificadoRepository;
  private final RazonCertificadoRepository razonCertificadoRepository;
  // @Autowired
  // private AuditoriaService auditoriaService;

  private static final Logger log = LoggerFactory.getLogger(CertificadoNoDeudaServiceImpl.class);

  public CertificadoNoDeudaServiceImpl(CertificadoNoDeudaRepository certificadoRepository,
      UserRepository usuarioRepository, PrestamoRepository prestamoRepository,
      BibliotecaRepository bibliotecaRepository,
      NotificacionService notificacionService, CertificadoDocxService certificadoDocxService,
      DocxToPdfConverter pdfConverter, SolicitudCertificadoRepository solicitudCertificadoRepository,
      RazonCertificadoRepository razonCertificadoRepository) {
    this.certificadoRepository = certificadoRepository;
    this.usuarioRepository = usuarioRepository;
    this.prestamoRepository = prestamoRepository;
    this.bibliotecaRepository = bibliotecaRepository;
    this.notificacionService = notificacionService;
    this.certificadoDocxService = certificadoDocxService;
    this.pdfConverter = pdfConverter;
    this.solicitudCertificadoRepository = solicitudCertificadoRepository;
    this.razonCertificadoRepository = razonCertificadoRepository;
  }

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
    Usuario solicitante = usuarioRepository.findById(solicitanteId)
        .orElseThrow(() -> new ResourceNotFoundException("Solicitante no encontrado"));

    // 5. Obtener solicitud existente o crear una nueva
    SolicitudCertificado solicitud;

    if (request.getSolicitudId() != null) {

      // CASO A: Generación a partir de una solicitud existente
      solicitud = solicitudCertificadoRepository.findById(request.getSolicitudId())
          .orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));

      // La solicitud debe corresponder a la biblioteca seleccionada
      if (!solicitud.getBiblioteca().getIdBiblioteca()
          .equals(biblioteca.getIdBiblioteca())) {
        throw new BusinessException(
            "La solicitud no pertenece a esta biblioteca");
      }

      // Adaptar este estado al flujo real de tu aplicación
      if (solicitud.getEstado() != EstadoSolicitud.APROBADA) {
        throw new BusinessException(
            "La solicitud debe estar aprobada para generar el certificado");
      }

      // Evitar duplicados
      if (certificadoRepository.existsBySolicitud_Id(solicitud.getId())) {
        throw new BusinessException(
            "Esta solicitud ya tiene un certificado generado");
      }

    } else {

      // CASO B: Generación directa por admin o bibliotecario
      if (!esAdmin && !esBibliotecario) {
        throw new BusinessException(
            "No tiene permisos para generar sin una solicitud previa");
      }

      solicitud = crearSolicitudDesdeRequest(
          request, biblioteca, solicitante);
    }

    // 6. Obtener titular y datos personales desde la solicitud
    Usuario usuario = solicitud.getUsuario();

    String ci = solicitud.getCi();

    // 7. Validar obligaciones del titular
    validarDeudas(
        usuario != null ? usuario.getId_usuario() : null,
        biblioteca.getIdBiblioteca(),
        ci);

    // 8. Crear certificado asociado obligatoriamente a la solicitud
    CertificadoNoDeuda certificado = new CertificadoNoDeuda();

    certificado.setSolicitud(solicitud);
    certificado.setBibliotecario(solicitante);
    certificado.setBiblioteca(biblioteca);
    certificado.setFechaEmision(LocalDateTime.now());
    certificado.setEstadoCertificado(EstadoCertificado.VIGENTE);

    if (request.getDiasValidez() != null) {
      certificado.setFechaVencimiento(
          LocalDateTime.now().plusDays(request.getDiasValidez()));
    }

    // 9. Guardar certificado
    CertificadoNoDeuda saved = certificadoRepository.save(certificado);

    // 10. Generar PDF
    // saved.setPdf_generado("pdfPath"); // Reemplazar por la ruta real
    saved = certificadoRepository.save(saved);

    // 11. Retornar respuesta
    return mapToResponse(saved);
    // ** Validar que no tenga sanciones activas
    // if (sancionRepository.hasUsuarioSancionesActivas(usuario.getId_usuario())) {
    // BigDecimal montoDeuda =
    // sancionRepository.sumMontoSancionesActivas(usuario.getId_usuario());
    // throw new BusinessException("El usuario tiene sanciones activas por un monto
    // de Bs. " + montoDeuda);
    // }

    // auditoriaService.registrar("GENERATE_CERTIFICATE", "no_debt_certificates",
    // saved.getId_certificado(), null, "Certificado generado");

    // notificacionService.enviarNotificacionCertificado(saved);

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

  // private String generarPDF(CertificadoNoDeuda certificado) {
  // // Aquí implementarías la generación del PDF usando iText, JasperReports,
  // etc.
  // // Por ahora retornamos una ruta simulada
  // return "/certificados/" + certificado.getIdCertificado() + ".pdf";
  // }

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
        ? certificadoRepository.findBySolicitud_Usuario_IdUsuarioAndBiblioteca_IdBiblioteca(usuarioId, bibliotecaId)
        : certificadoRepository.findBySolicitud_Usuario_IdUsuario(usuarioId);

    return certificados.stream().map(this::mapToResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<CertificadoResponse> findByCiAndBiblioteca(
      String ci,
      Long bibliotecaId) {
    List<CertificadoNoDeuda> certificados = certificadoRepository
        .findByBiblioteca_IdBibliotecaAndSolicitud_Ci(
            bibliotecaId, ci);
    return certificados.stream()
        .map(this::mapToResponse)
        .toList();
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
  public byte[] generarCertificadoPDF(
      Long id,
      Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) throws Exception, IOException {

    CertificadoResponse certificado = findByIdConAutorizacion(id, solicitanteId, authorities);
    // Obtener la solicitud asociada al certificado
    CertificadoNoDeuda entidad = certificadoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Certificado no encontrado con id: " + id));
    SolicitudCertificado solicitud = entidad.getSolicitud();
    UserCertificadoResponse userData = new UserCertificadoResponse();

    // Puede ser null si el titular no está registrado
    Usuario usuario = solicitud.getUsuario();

    userData.setUsuarioId(
        usuario != null ? usuario.getId_usuario() : null);
    userData.setBibliotecaId(certificado.getBibliotecaId());
    userData.setCi(solicitud.getCi());
    userData.setNombreCompleto(solicitud.getNombres() + " " + solicitud.getApellidos());
    userData.setNombreBiblioteca(certificado.getBibliotecaNombre());
    userData.setCodigoVerificacion(certificado.getCodigo_verificacion());
    userData.setFechaEmision(certificado.getFechaEmision());
    byte[] docx = certificadoDocxService.generar(userData);
    byte[] pdf = pdfConverter.convertir(docx);
    return pdf;
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
    SolicitudCertificado solicitud = certificado.getSolicitud();
    Usuario usuario = solicitud.getUsuario();
    // Long usuarioId = certificado.getSolicitud().getUsuario().getId_usuario();

    // 🔒 Reglas de acceso
    if (!esAdmin && !esBibliotecario && !usuario.getId_usuario().equals(solicitanteId)) {
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
    if (usuarioId == null) {
      return; // Opcional: puedes agregar una validación ligera por CI si tienes esa lógica
    }
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

  private void validarDeudas(Long usuarioId, Long bibliotecaId, String ci) {

    if (usuarioId != null) {
      // Validación normal por usuarioId (como arriba)
      validarDeudas(usuarioId, bibliotecaId);
    } else if (StringUtils.isNotBlank(ci)) {
      // Validación alternativa por CI (si tienes queries por CI)
      Long activos = prestamoRepository
          .countPrestamosConEstadoByCiAndBiblioteca(ci, bibliotecaId, EstadoPrestamo.ACTIVO);

      if (activos > 0) {
        throw new BusinessException(
            "La persona tiene " + activos +
                " préstamo(s) activo(s) en esta biblioteca");
      }

      Long vencidos = prestamoRepository
          .countPrestamosConEstadoByCiAndBiblioteca(
              ci,
              bibliotecaId,
              EstadoPrestamo.VENCIDO);

      if (vencidos > 0) {
        throw new BusinessException(
            "La persona tiene " + vencidos +
                " préstamo(s) vencido(s) en esta biblioteca");
      }

      Long renovados = prestamoRepository
          .countPrestamosConEstadoByCiAndBiblioteca(
              ci,
              bibliotecaId,
              EstadoPrestamo.RENOVADO);

      if (renovados > 0) {
        throw new BusinessException(
            "La persona tiene " + renovados +
                " préstamo(s) con renovación pendiente en esta biblioteca");
      }
    }
  }

  private boolean tieneRol(Collection<? extends GrantedAuthority> authorities, String rol) {
    return authorities.stream().anyMatch(a -> a.getAuthority().equals(rol));
  }

  private SolicitudCertificado crearSolicitudDesdeRequest(
      CertificadoRequest request,
      Biblioteca biblioteca,
      Usuario solicitante) {

    Usuario usuario = null;

    String nombres = request.getNombres();
    String apellidos = request.getApellidos();
    String ci = request.getCi();

    // Si la persona está registrada, utilizar sus datos
    if (request.getUsuarioId() != null) {
      usuario = usuarioRepository.findById(request.getUsuarioId())
          .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

      // Preferible utilizar campos separados de Persona,
      // si están disponibles, en lugar de dividir nombreCompleto.
      ci = usuario.getPersona().getCi();

      // Adaptar estas asignaciones a los campos reales de Persona.
      nombres = usuario.getPersona().getNombre();
      apellidos = usuario.getPersona().getApellido_pat() + " " + usuario.getPersona().getApellido_mat();

    } else {
      if (StringUtils.isBlank(nombres)
          || StringUtils.isBlank(apellidos)
          || StringUtils.isBlank(ci)) {
        throw new BusinessException(
            "Debe proporcionar nombres, apellidos y CI");
      }
    }

    RazonCertificado razon = razonCertificadoRepository
        .findById(request.getRazonCertificadoId())
        .orElseThrow(() -> new ResourceNotFoundException("Razón de certificado no encontrada"));

    SolicitudCertificado solicitud = new SolicitudCertificado();

    solicitud.setUsuario(usuario);
    solicitud.setBiblioteca(biblioteca);
    solicitud.setNombres(nombres);
    solicitud.setApellidos(apellidos);
    solicitud.setCi(ci);
    solicitud.setRazonCertificado(razon);
    solicitud.setFechaSolicitud(LocalDateTime.now());
    solicitud.setFechaRespuesta(LocalDateTime.now());
    solicitud.setAtendidoPor(solicitante);
    solicitud.setEstado(EstadoSolicitud.APROBADA);

    return solicitudCertificadoRepository.save(solicitud);
  }

  private CertificadoResponse mapToResponse(CertificadoNoDeuda certificado) {
    CertificadoResponse response = new CertificadoResponse();
    response.setId_certificado(certificado.getIdCertificado());
    response.setUsuario(mapToUsuarioSimple(certificado.getSolicitud().getUsuario()));
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
    response.setSolicitudId(certificado.getSolicitud().getId());
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