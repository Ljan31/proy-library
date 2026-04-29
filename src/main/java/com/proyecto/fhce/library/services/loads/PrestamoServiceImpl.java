package com.proyecto.fhce.library.services.loads;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.config.PrestamoProperties;
import com.proyecto.fhce.library.dto.ReglasPrestamoDTO;
import com.proyecto.fhce.library.dto.SancionDTO.EstadoSancionUsuarioDTO;
import com.proyecto.fhce.library.dto.request.loads.DevolucionRequest;
import com.proyecto.fhce.library.dto.request.loads.FiltroPrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.PrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.RenovacionRequest;
import com.proyecto.fhce.library.dto.response.library.BibliotecaSimpleResponse;
import com.proyecto.fhce.library.dto.response.library.EdicionSimpleResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.dto.response.library.LibroSimpleResponse;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionResueltaDTO;
import com.proyecto.fhce.library.dto.response.loads.PrestamoResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.HistorialEstadoEjemplar;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.TipoPrestamo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.mapper.PrestamoMapper;
import com.proyecto.fhce.library.repositories.BibliotecaEncargadoRepository;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.HistorialEstadoEjemplarRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
@Transactional(readOnly = true)
public class PrestamoServiceImpl implements PrestamoService {

  private static final Logger log = LoggerFactory.getLogger(PrestamoServiceImpl.class);

  @Autowired
  private PrestamoRepository prestamoRepository;

  @Autowired
  private EjemplarRepository ejemplarRepository;

  @Autowired
  private UserRepository usuarioRepository;

  @Autowired
  private BibliotecaRepository bibliotecaRepository;

  @Autowired
  private HistorialEstadoEjemplarRepository historialRepository;

  @Autowired
  private BibliotecaEncargadoRepository bibliotecaEncargadoRepository;

  @Autowired
  private PrestamoMapper prestamoMapper;

  // Integraciones con otros módulos
  @Autowired
  private ConfiguracionPrestamoService configuracionService;

  @Autowired
  private SancionService sancionService;
  // @Autowired
  // private NotificacionService notificacionService;

  // @Autowired
  // private AuditoriaService auditoriaService;

  // ==================== QUERIES ====================

  public List<PrestamoResponse> findAll() {
    return prestamoRepository.findAll()
        .stream()
        .map(prestamoMapper::toResponse)
        .collect(Collectors.toList());
  }

  public List<PrestamoResponse> findByBiblioteca(Long bibliotecaId, EstadoPrestamo estado) {
    if (!bibliotecaRepository.existsById(bibliotecaId)) {
      throw new ResourceNotFoundException("Biblioteca no encontrada con id: " + bibliotecaId);
    }

    List<Prestamo> prestamos = prestamoRepository
        .findByBibliotecaConFiltroEstado(bibliotecaId, estado);

    return prestamos.stream()
        .map(prestamoMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public PrestamoResponse findById(Long id) {
    Prestamo prestamo = prestamoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con id: " + id));
    return prestamoMapper.toResponse(prestamo);
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findByUsuario(Long usuarioId) {
    // Validate user exists
    if (!usuarioRepository.existsById(usuarioId)) {
      throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId);
    }
    return prestamoRepository.findByUsuario_IdUsuario(usuarioId)
        .stream()
        .map(prestamoMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findByEstado(EstadoPrestamo estado) {
    return prestamoRepository.findByEstadoPrestamo(estado)
        .stream()
        .map(prestamoMapper::toResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findPrestamosVencidos() {
    List<Prestamo> prestamos = prestamoRepository.findPrestamosVencidos(LocalDate.now());
    return prestamos.stream().map(prestamoMapper::toResponse).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<PrestamoResponse> findPrestamosPorVencer(int dias) {
    LocalDate fechaInicio = LocalDate.now();
    LocalDate fechaFin = LocalDate.now().plusDays(dias);
    List<Prestamo> prestamos = prestamoRepository.findPrestamosPorVencer(fechaInicio, fechaFin);
    return prestamos.stream().map(prestamoMapper::toResponse).collect(Collectors.toList());
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

    return prestamos.stream().map(prestamoMapper::toResponse).collect(Collectors.toList());
  }

  // ==================== OPERATIONS ====================
  @Transactional
  public PrestamoResponse realizarPrestamo(PrestamoRequest request, Long bibliotecarioId) {
    // ====== LOG ENTRADA ======
    log.info(
        "INICIO realizarPrestamo - usuarioId={}, ejemplarId={}, bibliotecaId={}, tipoPrestamo={}, fechaDevolucionEstimada={}, tipoDocumentoGarantia={}, condicionEntrega={}, observaciones={}, bibliotecarioId={}",
        request.getUsuarioId(),
        request.getEjemplarId(),
        request.getBibliotecaId(),
        request.getTipoPrestamo(),
        request.getFechaDevolucionEstimada(),
        request.getTipoDocumentoGarantia(),
        request.getCondicionEntrega(),
        request.getObservaciones(),
        bibliotecarioId);

    Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    Ejemplar ejemplar = ejemplarRepository.findById(request.getEjemplarId())
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado"));

    Biblioteca biblioteca = bibliotecaRepository.findById(request.getBibliotecaId())
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

    Usuario bibliotecario = validarEncargadoBiblioteca(bibliotecarioId, biblioteca);
    // Validate no active sanctions
    EstadoSancionUsuarioDTO estadoSanciones = sancionService.obtenerEstadoSanciones(usuario.getId_usuario());
    log.debug("Estado sanciones: {}", estadoSanciones);

    if (estadoSanciones.tieneSuspensionVigente()) {
      throw new BusinessException(
          "El usuario tiene suspensión activa hasta: " +
              estadoSanciones.fechaFinSuspensionMasProxima());
    }

    // Validate ejejmplar availability
    if (ejemplar.getEstadoEjemplar() != EstadoEjemplar.DISPONIBLE) {
      throw new BusinessException("El ejemplar no está disponible para préstamo");
    }

    // Check for an existing active loan on this exact copy (safety check)
    prestamoRepository.findPrestamoActivoByEjemplar(ejemplar.getIdEjemplar())
        .ifPresent(p -> {
          throw new BusinessException("El ejemplar ya tiene un préstamo activo registrado");
        });

    // ③ Resolver configuración aplicable — INTEGRACIÓN CON CONFIGURACIÓN
    ReglasPrestamoDTO reglas = configuracionService.obtenerReglas(
        biblioteca.getIdBiblioteca(),
        request.getTipoPrestamo() // TipoPrestamo.SALA o TipoPrestamo.DOMICILIO
    );
    log.debug(
        "Configuración aplicada - idConfig={}, tipoPrestamo={}, diasPrestamoMax={}, ejemplaresPermitidos={}, multaPorDia={}, multaMaxDias={}, diasSuspension={}, diasReserva={}, nivelAplicacion={}",
        reglas.getDiasPrestamoMax(),
        reglas.getEjemplaresPermitidos(),
        reglas.getMultaPorDia(),
        reglas.getMultaMaxDias(),
        reglas.getDiasSuspension(),
        reglas.getDiasReserva());
    // Validate simultaneous loan limit
    Long prestamosActivos = prestamoRepository.countPrestamosActivosByUsuario(usuario.getId_usuario());
    if (prestamosActivos >= reglas.getEjemplaresPermitidos()) {
      throw new BusinessException(
          "El usuario ha alcanzado el límite de préstamos simultáneos: "
              + reglas.getEjemplaresPermitidos());
    }

    // Create loan
    Prestamo prestamo = new Prestamo();
    prestamo.setEjemplar(ejemplar);
    prestamo.setUsuario(usuario);
    prestamo.setBiblioteca(biblioteca);
    prestamo.setBibliotecarioPrestamo(bibliotecario);
    prestamo.setFechaPrestamo(LocalDateTime.now());
    prestamo.setIdConfigUsado(reglas.getIdConfig());
    prestamo.setTipoPrestamo(request.getTipoPrestamo());
    // Calculate return date
    LocalDate fechaDevolucion = request.getFechaDevolucionEstimada() != null
        ? request.getFechaDevolucionEstimada()
        : LocalDate.now().plusDays(reglas.getDiasPrestamoMax());

    prestamo.setFechaDevolucionEstimada(fechaDevolucion);

    prestamo.setObservaciones(request.getObservaciones());
    prestamo.setEstadoPrestamo(EstadoPrestamo.ACTIVO);
    prestamo.setRenovaciones(0);
    prestamo.setTipoDocumentoGarantia(request.getTipoDocumentoGarantia());
    prestamo.setCondicionEntrega(request.getCondicionEntrega());
    // Update copy state
    ejemplar.setEstadoEjemplar(EstadoEjemplar.PRESTADO);
    ejemplarRepository.save(ejemplar);

    Prestamo saved = prestamoRepository.save(prestamo);
    log.info("Préstamo creado: id={} usuario={} ejemplar={}",
        saved.getIdPrestamo(), usuario.getId_usuario(), ejemplar.getIdEjemplar());

    // // Audit
    // auditoriaService.registrar("CREATE_LOAN", "loans", saved.getId_prestamo(),
    // null, "Préstamo creado para usuario: " + usuario.getUsername());

    // // Notification
    // notificacionService.enviarNotificacionPrestamo(saved);
    return prestamoMapper.toResponse(saved);
  }

  @Transactional
  public PrestamoResponse realizarDevolucion(DevolucionRequest request, Long bibliotecarioId) {
    Prestamo prestamo = prestamoRepository.findById(request.getPrestamoId())
        .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));

    if (prestamo.getEstadoPrestamo() != EstadoPrestamo.ACTIVO
        && prestamo.getEstadoPrestamo() != EstadoPrestamo.VENCIDO
        && prestamo.getEstadoPrestamo() != EstadoPrestamo.RENOVADO) {
      throw new BusinessException("El préstamo no está en un estado válido para devolución. Estado actual: "
          + prestamo.getEstadoPrestamo());
      // throw new BusinessException("Estado inválido para devolución: " +
      // prestamo.getEstadoPrestamo());
    }

    Usuario bibliotecario = validarEncargadoBiblioteca(bibliotecarioId, prestamo.getBiblioteca());

    // Register return
    prestamo.setFechaDevolucionReal(LocalDateTime.now());
    prestamo.setBibliotecarioDevolucion(bibliotecario);
    prestamo.setEstadoPrestamo(EstadoPrestamo.DEVUELTO);
    prestamo.setCondicionDevolucion(request.getCondicionDevolucion());

    if (request.getObservaciones() != null) {
      String obs = prestamo.getObservaciones() != null
          ? prestamo.getObservaciones() + ". " + request.getObservaciones()
          : request.getObservaciones();
      prestamo.setObservaciones(obs);
    }

    if (prestamo.getCondicionEntrega() != null &&
        request.getCondicionDevolucion().ordinal() > prestamo.getCondicionEntrega().ordinal()) {
      // La condición empeoró — podrías registrar en observaciones automáticamente
      String alertaDeterioro = "[ALERTA] El ejemplar fue devuelto en peor condición que la entregada. " +
          "Entrega: " + prestamo.getCondicionEntrega() + " → Devolución: " + request.getCondicionDevolucion();
      prestamo.setObservaciones(
          (prestamo.getObservaciones() != null ? prestamo.getObservaciones() + ". " : "") + alertaDeterioro);
    }

    // Update copy state
    Ejemplar ejemplar = prestamo.getEjemplar();
    EstadoEjemplar estadoAnterior = ejemplar.getEstadoEjemplar();
    EstadoEjemplar nuevoEstado = request.getEstadoEjemplar() != null
        ? request.getEstadoEjemplar()
        : EstadoEjemplar.DISPONIBLE;
    ejemplar.setEstadoEjemplar(nuevoEstado);
    ejemplarRepository.save(ejemplar);

    registrarCambioEstado(ejemplar, estadoAnterior, nuevoEstado, request.getObservaciones(), bibliotecario);
    Prestamo updated = prestamoRepository.save(prestamo);

    // ⑥ Delegar sanción al módulo de sanciones si hubo retraso — INTEGRACIÓN
    // La sanción se procesa FUERA del bloque transaccional principal del préstamo.
    // SancionService maneja su propia transacción y garantiza idempotencia.
    // if (LocalDate.now().isAfter(prestamo.getFechaDevolucionEstimada())) {
    // try {
    // sancionService.procesarDevolucionTardia(updated.getIdPrestamo());
    // log.info("Sanción procesada para préstamo vencido id={}",
    // updated.getIdPrestamo());
    // } catch (Exception e) {
    // // Un fallo en la sanción NO revierte la devolución — el préstamo ya está
    // // cerrado.
    // // El CRON de SancionService retomará este préstamo en su próxima ejecución.
    // log.error("Error al procesar sanción para préstamo id={}: {}",
    // updated.getIdPrestamo(), e.getMessage());
    // }
    // }

    if (prestamo.getTipoPrestamo() == TipoPrestamo.DOMICILIO
        && LocalDate.now().isAfter(prestamo.getFechaDevolucionEstimada())) {
      try {
        sancionService.procesarDevolucionTardia(updated.getIdPrestamo());
        log.info("Sanción procesada para préstamo id={}", updated.getIdPrestamo());
      } catch (Exception e) {
        log.error("Error al procesar sanción para préstamo id={}: {}",
            updated.getIdPrestamo(), e.getMessage());
      }
    }

    // // Audit
    // auditoriaService.registrar("RETURN_LOAN", "loans", updated.getId_prestamo(),
    // null, "Devolución registrada por bibliotecario: " +
    // bibliotecario.getUsername());

    // // Notification
    // notificacionService.enviarNotificacionDevolucion(updated);
    return prestamoMapper.toResponse(updated);
  }

  @Transactional
  public PrestamoResponse renovarPrestamo(RenovacionRequest request, Long userId,
      Collection<? extends GrantedAuthority> authorities) {
    System.out.println("\n===== INICIO RENOVACION =====");

    // 1. Entrada
    System.out.println("Request prestamoId: " + request.getPrestamoId());
    System.out.println("User ID: " + userId);

    Prestamo prestamo = prestamoRepository.findById(request.getPrestamoId())
        .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado"));
    System.out.println("\n[DATOS PRESTAMO]");
    System.out.println("ID: " + prestamo.getIdPrestamo());
    System.out.println("Usuario ID: " + prestamo.getUsuario().getId_usuario());
    System.out.println("Estado: " + prestamo.getEstadoPrestamo());
    System.out.println("Fecha devolución: " + prestamo.getFechaDevolucionEstimada());
    System.out.println("Renovaciones actuales: " + prestamo.getRenovaciones());

    boolean esBibliotecario = authorities.stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_BIBLIOTECARIO"));

    System.out.println("===== DEBUG RENOVACION =====");
    System.out.println("User ID: " + userId);

    System.out.println("Roles del usuario:");
    authorities.forEach(auth -> System.out.println(" - " + auth.getAuthority()));

    System.out.println("============================");

    boolean esPropietario = prestamo.getUsuario()
        .getId_usuario()
        .equals(userId);
    System.out.println("\n¿Es propietario?: " + esPropietario);
    if (!esPropietario) {
      if (esBibliotecario) {
        validarEncargadoBiblioteca(userId, prestamo.getBiblioteca());
      } else {
        throw new BusinessException("No está autorizado para renovar este préstamo");
      }
    }

    if (prestamo.getEstadoPrestamo() != EstadoPrestamo.ACTIVO
        && prestamo.getEstadoPrestamo() != EstadoPrestamo.RENOVADO) {
      throw new BusinessException("El préstamo no está activo y no puede renovarse. Estado actual: "
          + prestamo.getEstadoPrestamo());
    }
    // 6. Validar vencimiento
    System.out.println("\n[VALIDANDO FECHA]");
    System.out.println("Hoy: " + LocalDate.now());
    System.out.println("Fecha devolución: " + prestamo.getFechaDevolucionEstimada());
    // Validate not overdue
    if (LocalDate.now().isAfter(prestamo.getFechaDevolucionEstimada())) {
      throw new BusinessException("No se puede renovar un préstamo vencido. Debe registrar la devolución.");
    }

    // ⑦ Verificar suspensión vigente antes de renovar — INTEGRACIÓN CON SANCIONES
    EstadoSancionUsuarioDTO estadoSanciones = sancionService
        .obtenerEstadoSanciones(prestamo.getUsuario().getId_usuario());
    if (estadoSanciones.tieneSuspensionVigente()) {
      throw new BusinessException("No se puede renovar con suspensión activa");
    }

    // ⑧ Validar límite de renovaciones desde configuración — INTEGRACIÓN CON CONFIG
    ReglasPrestamoDTO reglas = configuracionService.obtenerReglas(
        prestamo.getBiblioteca().getIdBiblioteca(),
        prestamo.getTipoPrestamo() // necesitas este campo en la entidad (ver punto 3)
    );
    int renovacionesMax = reglas.getRenovacionesMax();
    // Validate renewal limit
    if (prestamo.getRenovaciones() >= renovacionesMax) {
      throw new BusinessException(
          "Ha alcanzado el límite de renovaciones: " + renovacionesMax);
    }
    // Renew
    prestamo.setRenovaciones(prestamo.getRenovaciones() + 1);
    prestamo.setFechaDevolucionEstimada(
        prestamo.getFechaDevolucionEstimada().plusDays(reglas.getDiasPrestamoMax()));
    prestamo.setEstadoPrestamo(EstadoPrestamo.RENOVADO);

    Prestamo renewed = prestamoRepository.save(prestamo);
    log.info("Préstamo renovado: id={} renovación #{}", renewed.getIdPrestamo(), renewed.getRenovaciones());

    // // Audit
    // auditoriaService.registrar("RENEW_LOAN", "loans", renewed.getId_prestamo(),
    // null, "Renovación #" + renewed.getRenovaciones() + ": " +
    // request.getMotivo());

    // // Notification
    // notificacionService.enviarNotificacionRenovacion(renewed);

    return prestamoMapper.toResponse(renewed);
  }

  // ==================== SCHEDULED JOBS ====================
  /**
   * Marca préstamos vencidos como VENCIDO.
   * El CRON de SancionService (02:00 AM) procesará las sanciones automáticamente
   * usando findPrestamosVencidosSinSancion() — no duplicar lógica aquí.
   */
  @Scheduled(cron = "0 0 1 * * *") // Every day at 8 AM
  @Transactional
  public void procesarPrestamosVencidos() {
    List<Prestamo> vencidos = prestamoRepository.findPrestamosVencidos(LocalDate.now());
    int procesados = 0;
    for (Prestamo prestamo : vencidos) {
      if (prestamo.getEstadoPrestamo() == EstadoPrestamo.ACTIVO
          || prestamo.getEstadoPrestamo() == EstadoPrestamo.RENOVADO) {
        prestamo.setEstadoPrestamo(EstadoPrestamo.VENCIDO);
        prestamoRepository.save(prestamo);
        procesados++;
        // notificacionService.enviarNotificacionVencimiento(prestamo);
      }
    }
    log.info("CRON marcarPrestamosVencidos: {} préstamos marcados como VENCIDO", procesados);

  }

  @Scheduled(cron = "0 0 9 * * *") // Every day at 9 AM
  public void notificarPrestamosPorVencer() {
    // Notify loans expiring in the next 2 days
    List<Prestamo> porVencer = prestamoRepository.findPrestamosPorVencer(
        LocalDate.now(),
        LocalDate.now().plusDays(2));
    log.info("CRON notificarPrestamosPorVencer: {} préstamos por vencer", porVencer.size());

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

  private Usuario validarEncargadoBiblioteca(Long bibliotecarioId, Biblioteca biblioteca) {
    Usuario bibliotecario = usuarioRepository.findById(bibliotecarioId)
        .orElseThrow(() -> new ResourceNotFoundException("Bibliotecario no encontrado"));

    boolean esEncargado = bibliotecaEncargadoRepository
        .existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(
            biblioteca.getIdBiblioteca(), bibliotecario.getId_usuario());

    if (!esEncargado) {

      throw new BusinessException(
          "El bibliotecario no está autorizado para operar en esta biblioteca");
    }

    return bibliotecario;
  }

  private void registrarCambioEstado(Ejemplar ejemplar, EstadoEjemplar estadoAnterior,
      EstadoEjemplar estadoNuevo, String motivo, Usuario usuario) {
    HistorialEstadoEjemplar historial = new HistorialEstadoEjemplar();
    historial.setEjemplar(ejemplar);
    historial.setEstadoAnterior(estadoAnterior);
    historial.setEstadoNuevo(estadoNuevo);
    historial.setMotivo(motivo);
    // Obtener el usuario actual
    historial.setUsuarioCambio(usuario);

    historialRepository.save(historial);
  }

}