package com.proyecto.fhce.library.services.loads;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.config.PrestamoProperties;
import com.proyecto.fhce.library.dto.request.loads.DevolucionRequest;
import com.proyecto.fhce.library.dto.request.loads.FiltroPrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.PrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.RenovacionRequest;
import com.proyecto.fhce.library.dto.response.library.BibliotecaSimpleResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.dto.response.library.LibroSimpleResponse;
import com.proyecto.fhce.library.dto.response.loads.PrestamoResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
@Transactional
public class PrestamoServiceImpl {

  @Autowired
  private PrestamoRepository prestamoRepository;

  @Autowired
  private EjemplarRepository ejemplarRepository;

  @Autowired
  private UserRepository usuarioRepository;

  @Autowired
  private BibliotecaRepository bibliotecaRepository;

  @Autowired
  private PrestamoProperties prestamoProperties;
  // @Autowired
  // private ConfiguracionPrestamoRepository configuracionRepository;

  // @Autowired
  // private SancionRepository sancionRepository;

  // @Autowired
  // private NotificacionService notificacionService;

  // @Autowired
  // private AuditoriaService auditoriaService;

  // ==================== QUERIES ====================

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findAll() {
    return prestamoRepository.findAll()
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public PrestamoResponse findById(Long id) {
    Prestamo prestamo = prestamoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con id: " + id));
    return mapToResponse(prestamo);
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findByUsuario(Long usuarioId) {
    // Validate user exists
    if (!usuarioRepository.existsById(usuarioId)) {
      throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId);
    }
    return prestamoRepository.findByUsuario_IdUsuario(usuarioId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findByEstado(EstadoPrestamo estado) {
    return prestamoRepository.findByEstadoPrestamo(estado)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findPrestamosVencidos() {
    List<Prestamo> prestamos = prestamoRepository.findPrestamosVencidos(LocalDate.now());
    return prestamos.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findPrestamosPorVencer(int dias) {
    LocalDate fechaInicio = LocalDate.now();
    LocalDate fechaFin = LocalDate.now().plusDays(dias);
    List<Prestamo> prestamos = prestamoRepository.findPrestamosPorVencer(fechaInicio, fechaFin);
    return prestamos.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> filtrar(FiltroPrestamoRequest filtro) {
    // Base query using available repository methods; extend with Specifications for
    // complex filtering
    List<Prestamo> prestamos;

    if (filtro.getBibliotecaId() != null && filtro.getFechaDesde() != null && filtro.getFechaHasta() != null) {
      prestamos = prestamoRepository.findByBibliotecaAndFechaBetween(
          filtro.getBibliotecaId(),
          filtro.getFechaDesde().atStartOfDay(),
          filtro.getFechaHasta().atTime(23, 59, 59));
    } else if (filtro.getUsuarioId() != null && filtro.getEstadoPrestamo() != null) {
      prestamos = prestamoRepository.findByUsuarioAndEstado(filtro.getUsuarioId(), filtro.getEstadoPrestamo());
    } else if (filtro.getUsuarioId() != null) {
      prestamos = prestamoRepository.findByUsuario_IdUsuario(filtro.getUsuarioId());
    } else if (filtro.getEstadoPrestamo() != null) {
      prestamos = prestamoRepository.findByEstadoPrestamo(filtro.getEstadoPrestamo());
    } else if (filtro.getBibliotecaId() != null) {
      prestamos = prestamoRepository.findByBiblioteca_IdBiblioteca(filtro.getBibliotecaId());
    } else {
      prestamos = prestamoRepository.findAll();
    }

    return prestamos.stream().map(this::mapToResponse).collect(Collectors.toList());
  }

  // ==================== OPERATIONS ====================

  public PrestamoResponse realizarPrestamo(PrestamoRequest request, Long bibliotecarioId) {
    Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    Ejemplar ejemplar = ejemplarRepository.findById(request.getEjemplarId())
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado"));

    Biblioteca biblioteca = bibliotecaRepository.findById(request.getBibliotecaId())
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

    Usuario bibliotecario = usuarioRepository.findById(bibliotecarioId)
        .orElseThrow(() -> new ResourceNotFoundException("Bibliotecario no encontrado"));

    // // Validate no active sanctions
    // if (sancionRepository.hasUsuarioSancionesActivas(usuario.getId_usuario())) {
    // throw new BusinessException("El usuario tiene sanciones activas y no puede
    // realizar préstamos");
    // }

    // Validate ejejmplar availability
    if (ejemplar.getEstadoEjemplar() != EstadoEjemplar.DISPONIBLE) {
      throw new BusinessException("El ejemplar no está disponible para préstamo");
    }

    // Check for an existing active loan on this exact copy (safety check)
    prestamoRepository.findPrestamoActivoByEjemplar(ejemplar.getIdEjemplar())
        .ifPresent(p -> {
          throw new BusinessException("El ejemplar ya tiene un préstamo activo registrado");
        });

    // // Get loan configuration
    // ConfiguracionPrestamo config = obtenerConfiguracion(usuario, biblioteca);

    // Validate simultaneous loan limit
    Long prestamosActivos = prestamoRepository.countPrestamosActivosByUsuario(usuario.getId_usuario());
    if (prestamosActivos >= prestamoProperties.getEjemplaresSimultaneosMaximos()) {
      throw new BusinessException(
          "El usuario ha alcanzado el límite de préstamos simultáneos: "
              + prestamoProperties.getEjemplaresSimultaneosMaximos());
    }

    // Create loan
    Prestamo prestamo = new Prestamo();
    prestamo.setEjemplar(ejemplar);
    prestamo.setUsuario(usuario);
    prestamo.setBiblioteca(biblioteca);
    prestamo.setBibliotecarioPrestamo(bibliotecario);
    prestamo.setFechaPrestamo(LocalDateTime.now());

    // Calculate return date
    LocalDate fechaDevolucion = request.getFechaDevolucionEstimada() != null
        ? request.getFechaDevolucionEstimada()
        : LocalDate.now().plusDays(prestamoProperties.getDiasMaximos());

    prestamo.setFechaDevolucionEstimada(fechaDevolucion);

    prestamo.setObservaciones(request.getObservaciones());
    prestamo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
    prestamo.setRenovaciones(0);

    // Update copy state
    ejemplar.setEstadoEjemplar(EstadoEjemplar.PRESTADO);
    ejemplarRepository.save(ejemplar);

    Prestamo saved = prestamoRepository.save(prestamo);

    // // Audit
    // auditoriaService.registrar("CREATE_LOAN", "loans", saved.getId_prestamo(),
    // null, "Préstamo creado para usuario: " + usuario.getUsername());

    // // Notification
    // notificacionService.enviarNotificacionPrestamo(saved);

    return mapToResponse(saved);
  }

  public PrestamoResponse realizarDevolucion(DevolucionRequest request, Long bibliotecarioId) {
    Prestamo prestamo = prestamoRepository.findById(request.getPrestamoId())
        .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));

    if (prestamo.getEstadoPrestamo() != EstadoPrestamo.ACTIVO
        && prestamo.getEstadoPrestamo() != EstadoPrestamo.VENCIDO
        && prestamo.getEstadoPrestamo() != EstadoPrestamo.RENOVADO) {
      throw new BusinessException("El préstamo no está en un estado válido para devolución. Estado actual: "
          + prestamo.getEstadoPrestamo());
    }

    Usuario bibliotecario = usuarioRepository.findById(bibliotecarioId)
        .orElseThrow(() -> new ResourceNotFoundException("Bibliotecario no encontrado"));

    // Register return
    prestamo.setFechaDevolucionReal(LocalDateTime.now());
    prestamo.setBibliotecarioDevolucion(bibliotecario);
    prestamo.setEstadoPrestamo(EstadoPrestamo.DEVUELTO);

    if (request.getObservaciones() != null) {
      prestamo.setObservaciones(
          (prestamo.getObservaciones() != null ? prestamo.getObservaciones() + ". " : "")
              + request.getObservaciones());
    }

    // Update copy state
    Ejemplar ejemplar = prestamo.getEjemplar();
    EstadoEjemplar nuevoEstado = request.getEstadoEjemplar() != null
        ? request.getEstadoEjemplar()
        : EstadoEjemplar.DISPONIBLE;
    ejemplar.setEstadoEjemplar(nuevoEstado);
    ejemplarRepository.save(ejemplar);

    // Check for overdue and create sanction if needed
    // if (LocalDate.now().isAfter(prestamo.getFechaDevolucionEstimada())) {
    // crearSancionPorRetraso(prestamo);
    // }

    Prestamo updated = prestamoRepository.save(prestamo);

    // // Audit
    // auditoriaService.registrar("RETURN_LOAN", "loans", updated.getId_prestamo(),
    // null, "Devolución registrada por bibliotecario: " +
    // bibliotecario.getUsername());

    // // Notification
    // notificacionService.enviarNotificacionDevolucion(updated);

    return mapToResponse(updated);
  }

  public PrestamoResponse renovarPrestamo(RenovacionRequest request) {
    Prestamo prestamo = prestamoRepository.findById(request.getPrestamoId())
        .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));

    if (prestamo.getEstadoPrestamo() != EstadoPrestamo.ACTIVO
        && prestamo.getEstadoPrestamo() != EstadoPrestamo.RENOVADO) {
      throw new BusinessException("El préstamo no está activo y no puede renovarse. Estado actual: "
          + prestamo.getEstadoPrestamo());
    }

    // Validate not overdue
    if (LocalDate.now().isAfter(prestamo.getFechaDevolucionEstimada())) {
      throw new BusinessException("No se puede renovar un préstamo vencido. Debe registrar la devolución.");
    }

    // // Get configuration
    // ConfiguracionPrestamo config = obtenerConfiguracion(
    // prestamo.getUsuario(),
    // prestamo.getBiblioteca());

    // Validate renewal limit
    // if (prestamo.getRenovaciones() >= config.getRenovacionesMax()) {
    if (prestamo.getRenovaciones() >= prestamoProperties.getRenovacionesMaximas()) {
      throw new BusinessException(
          "Ha alcanzado el límite de renovaciones: " + prestamoProperties.getRenovacionesMaximas());
    }

    // // Validate no active sanctions
    // if
    // (sancionRepository.hasUsuarioSancionesActivas(prestamo.getUsuario().getId_usuario()))
    // {
    // throw new BusinessException("No se puede renovar con sanciones activas");
    // }

    // Renew
    prestamo.setRenovaciones(prestamo.getRenovaciones() + 1);
    prestamo.setFechaDevolucionEstimada(
        prestamo.getFechaDevolucionEstimada().plusDays(prestamoProperties.getDiasMaximos()));
    prestamo.setEstadoPrestamo(EstadoPrestamo.RENOVADO);

    Prestamo renewed = prestamoRepository.save(prestamo);

    // // Audit
    // auditoriaService.registrar("RENEW_LOAN", "loans", renewed.getId_prestamo(),
    // null, "Renovación #" + renewed.getRenovaciones() + ": " +
    // request.getMotivo());

    // // Notification
    // notificacionService.enviarNotificacionRenovacion(renewed);

    return mapToResponse(renewed);
  }

  // ==================== SCHEDULED JOBS ====================

  @Scheduled(cron = "0 0 8 * * *") // Every day at 8 AM
  public void procesarPrestamosVencidos() {
    List<Prestamo> vencidos = prestamoRepository.findPrestamosVencidos(LocalDate.now());

    for (Prestamo prestamo : vencidos) {
      if (prestamo.getEstadoPrestamo() == EstadoPrestamo.ACTIVO
          || prestamo.getEstadoPrestamo() == EstadoPrestamo.RENOVADO) {
        prestamo.setEstadoPrestamo(EstadoPrestamo.VENCIDO);
        prestamoRepository.save(prestamo);
        // notificacionService.enviarNotificacionVencimiento(prestamo);
      }
    }
  }

  @Scheduled(cron = "0 0 9 * * *") // Every day at 9 AM
  public void notificarPrestamosPorVencer() {
    // Notify loans expiring in the next 2 days
    List<Prestamo> porVencer = prestamoRepository.findPrestamosPorVencer(
        LocalDate.now(),
        LocalDate.now().plusDays(2));

    // for (Prestamo prestamo : porVencer) {
    // notificacionService.enviarNotificacionProximoVencimiento(prestamo);
    // }
  }

  // ==================== PRIVATE HELPERS ====================

  // private ConfiguracionPrestamo obtenerConfiguracion(Usuario usuario,
  // Biblioteca biblioteca) {
  // // Try user-specific config first, then library-level, then global default
  // return configuracionRepository
  // .findByUsuarioTipoAndBiblioteca(usuario.getTipoUsuario(),
  // biblioteca.getIdBiblioteca())
  // .orElseGet(() -> configuracionRepository
  // .findDefaultByBiblioteca(biblioteca.getIdBiblioteca())
  // .orElseGet(() -> configuracionRepository
  // .findGlobalDefault()
  // .orElseThrow(() -> new BusinessException(
  // "No se encontró configuración de préstamo para esta biblioteca"))));
  // }

  // private void crearSancionPorRetraso(Prestamo prestamo) {
  // long diasRetraso = java.time.temporal.ChronoUnit.DAYS.between(
  // prestamo.getFechaDevolucionEstimada(),
  // LocalDate.now());

  // if (diasRetraso <= 0)
  // return;

  // Sancion sancion = new Sancion();
  // sancion.setUsuario(prestamo.getUsuario());
  // sancion.setPrestamo(prestamo);
  // sancion.setTipoSancion(TipoSancion.RETRASO_DEVOLUCION);
  // sancion.setDiasRetraso((int) diasRetraso);
  // sancion.setFechaInicio(LocalDate.now());
  // sancion.setFechaFin(LocalDate.now().plusDays(diasRetraso));
  // sancion.setDescripcion("Sanción automática por retraso de " + diasRetraso + "
  // día(s)");
  // sancion.setActiva(true);

  // sancionRepository.save(sancion);
  // notificacionService.enviarNotificacionSancion(sancion);

  // auditoriaService.registrar("CREATE_SANCTION", "sanciones", sancion.getId(),
  // null, "Sanción creada por retraso de " + diasRetraso + " día(s) en préstamo
  // #"
  // + prestamo.getId_prestamo());
  // }

  private PrestamoResponse mapToResponse(Prestamo prestamo) {
    PrestamoResponse response = new PrestamoResponse();
    response.setId_prestamo(prestamo.getId_prestamo());
    response.setEstadoPrestamo(prestamo.getEstadoPrestamo());
    response.setFechaPrestamo(prestamo.getFechaPrestamo());
    response.setFechaDevolucionEstimada(prestamo.getFechaDevolucionEstimada());
    response.setFechaDevolucionReal(prestamo.getFechaDevolucionReal());
    response.setRenovaciones(prestamo.getRenovaciones());
    response.setObservaciones(prestamo.getObservaciones());

    // Map Ejemplar (eagerly needed fields)
    if (prestamo.getEjemplar() != null) {
      EjemplarResponse ejemplarResponse = new EjemplarResponse();
      ejemplarResponse.setId_ejemplar(prestamo.getEjemplar().getIdEjemplar());
      ejemplarResponse.setCodigo_ejemplar(prestamo.getEjemplar().getCodigoEjemplar());
      ejemplarResponse.setEstadoEjemplar(prestamo.getEjemplar().getEstadoEjemplar());

      if (prestamo.getEjemplar().getLibro() != null) {
        LibroSimpleResponse libroResponse = new LibroSimpleResponse();
        libroResponse.setId_libro(prestamo.getEjemplar().getLibro().getIdLibro());
        libroResponse.setTitulo(prestamo.getEjemplar().getLibro().getTitulo());
        ejemplarResponse.setLibro(libroResponse);
      }

      response.setEjemplar(ejemplarResponse);
    }

    // ===================== USUARIO =====================
    if (prestamo.getUsuario() != null) {
      UsuarioSimpleResponse usuarioResponse = new UsuarioSimpleResponse();
      usuarioResponse.setId_usuario(prestamo.getUsuario().getId_usuario());
      usuarioResponse.setNombreCompleto(
          prestamo.getUsuario().getPersona().getApellido_pat() + " " + prestamo.getUsuario().getPersona().getNombre());
      usuarioResponse.setUsername(prestamo.getUsuario().getUsername());

      response.setUsuario(usuarioResponse);
    }
    // ===================== BIBLIOTECA =====================
    if (prestamo.getBiblioteca() != null) {
      BibliotecaSimpleResponse bibliotecaResponse = new BibliotecaSimpleResponse();
      bibliotecaResponse.setId_biblioteca(prestamo.getBiblioteca().getIdBiblioteca());
      bibliotecaResponse.setNombre(prestamo.getBiblioteca().getNombre());

      response.setBiblioteca(bibliotecaResponse);
    }

    // Map librarians
    // ===================== BIBLIOTECARIO PRESTAMO =====================
    if (prestamo.getBibliotecarioPrestamo() != null) {
      UsuarioSimpleResponse bibliotecarioPrestamo = new UsuarioSimpleResponse();
      bibliotecarioPrestamo.setId_usuario(prestamo.getBibliotecarioPrestamo().getId_usuario());
      bibliotecarioPrestamo.setNombreCompleto(prestamo.getBibliotecarioPrestamo().getPersona().getApellido_pat() + " "
          + prestamo.getBibliotecarioPrestamo().getPersona().getNombre());
      bibliotecarioPrestamo.setUsername(prestamo.getBibliotecarioPrestamo().getUsername());

      response.setBibliotecarioPrestamo(bibliotecarioPrestamo);
    }

    // ===================== BIBLIOTECARIO DEVOLUCION =====================
    if (prestamo.getBibliotecarioDevolucion() != null) {
      UsuarioSimpleResponse bibliotecarioDevolucion = new UsuarioSimpleResponse();
      bibliotecarioDevolucion.setId_usuario(prestamo.getBibliotecarioDevolucion().getId_usuario());
      bibliotecarioDevolucion.setNombreCompleto(prestamo.getBibliotecarioDevolucion().getPersona().getApellido_pat()
          + " " + prestamo.getBibliotecarioDevolucion().getPersona().getNombre());
      bibliotecarioDevolucion.setUsername(prestamo.getBibliotecarioDevolucion().getUsername());

      response.setBibliotecarioDevolucion(bibliotecarioDevolucion);
    }

    // Convenience flag: is overdue?
    if (prestamo.getEstadoPrestamo() == EstadoPrestamo.ACTIVO
        || prestamo.getEstadoPrestamo() == EstadoPrestamo.RENOVADO) {
      response.setVencido(LocalDate.now().isAfter(prestamo.getFechaDevolucionEstimada()));
    } else {
      response.setVencido(false);
    }

    return response;
  }
}