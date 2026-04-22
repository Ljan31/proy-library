package com.proyecto.fhce.library.services.loads;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.SancionDTO.CondonacionRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.EstadoSancionUsuarioDTO;
import com.proyecto.fhce.library.dto.SancionDTO.PagoMultaRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.SancionManualRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.SancionResponseDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionResueltaDTO;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.MotivoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.exception.SancionException.PrestamoNoVencidoException;
import com.proyecto.fhce.library.exception.SancionException.SancionDuplicadaException;
import com.proyecto.fhce.library.exception.SancionException.SancionNoActivaException;
import com.proyecto.fhce.library.exception.SancionException.SancionNotFoundException;
import com.proyecto.fhce.library.mapper.SancionMapper;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.SancionRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class SancionService {

  private static final Logger log = LoggerFactory.getLogger(SancionService.class);

  private final SancionRepository sancionRepository;
  private final PrestamoRepository prestamoRepository;
  private final UserRepository usuarioRepository;
  private final BibliotecaRepository bibliotecaRepository;
  private final ConfiguracionPrestamoService configuracionService;
  // private final NotificacionService notificacionService;
  private final SancionMapper mapper;

  public SancionService(
      SancionRepository sancionRepository,
      PrestamoRepository prestamoRepository,
      UserRepository usuarioRepository,
      BibliotecaRepository bibliotecaRepository,
      ConfiguracionPrestamoService configuracionService,
      // NotificacionService notificacionService,
      SancionMapper mapper) {
    this.sancionRepository = sancionRepository;
    this.prestamoRepository = prestamoRepository;
    this.usuarioRepository = usuarioRepository;
    this.bibliotecaRepository = bibliotecaRepository;
    this.configuracionService = configuracionService;
    // this.notificacionService = notificacionService;
    this.mapper = mapper;
  }

  // ════════════════════════════════════════════════════════════════════════
  // GENERACIÓN AUTOMÁTICA — llamada por el módulo de préstamos al devolver
  // ════════════════════════════════════════════════════════════════════════

  /**
   * Procesa un préstamo devuelto con retraso.
   * Calcula multa y suspensión según la configuración que estaba vigente
   * al momento del préstamo (usando el idConfig guardado en el préstamo).
   *
   * Transaccional: si falla la notificación, NO revierte la sanción.
   * La sanción ya es un hecho — la notificación es un efecto secundario.
   */
  @Transactional
  public SancionResponseDTO procesarDevolucionTardia(Long idPrestamo) {
    Prestamo prestamo = prestamoRepository.findByIdWithRelations(idPrestamo)
        .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

    // ① Verificar que no exista sanción activa ya creada para este préstamo
    if (sancionRepository.existsByPrestamo_IdPrestamoAndEstado(idPrestamo, EstadoSancion.ACTIVA)) {
      throw new SancionDuplicadaException(idPrestamo);
    }

    // ② Calcular días de retraso
    int diasRetraso = calcularDiasRetraso(prestamo);
    if (diasRetraso <= 0) {
      throw new PrestamoNoVencidoException(idPrestamo);
    }

    // ③ Obtener monto de multa desde ConfiguracionPrestamoService
    // Usamos el idConfig guardado en el préstamo para respetar la config histórica
    BigDecimal montoMulta = configuracionService.calcularMulta(
        prestamo.getIdConfigUsado(), diasRetraso);

    // ④ Determinar si corresponde suspensión
    boolean debeSuspender = configuracionService.debeSuspender(
        prestamo.getIdConfigUsado(), diasRetraso);

    // ⑤ Obtener configuración completa si hay suspensión (para los días)
    int diasSuspension = 0;
    if (debeSuspender) {
      // ConfiguracionResueltaDTO config =
      // configuracionService.resolverConfiguracionAplicable(
      // prestamo.getBiblioteca().getIdBiblioteca(),
      // obtenerRolIdDelUsuario(prestamo.getUsuario()),
      // prestamo.getTipoPrestamo());
      // diasSuspension = config.diasSuspension();
      ConfiguracionResueltaDTO config = configuracionService
          .obtenerPorId(prestamo.getIdConfigUsado());

      // int diasSuspension = debeSuspender ? config.getDiasSuspension() : 0;
      diasSuspension = config.getDiasSuspension();
    }

    // ⑥ Construir y persistir la sanción
    Sancion sancion = construirSancionPorRetraso(
        prestamo, diasRetraso, montoMulta, diasSuspension, debeSuspender);
    Sancion guardada = sancionRepository.save(sancion);

    log.info("Sanción creada [id={}] para préstamo [id={}], usuario [id={}], diasRetraso={}",
        guardada.getIdSancion(), idPrestamo, prestamo.getUsuario().getId_usuario(), diasRetraso);

    // ⑦ Notificar al usuario (no transaccional — si falla, no revierte la sanción)
    enviarNotificacionSancion(guardada);

    return mapper.toResponseDTO(guardada);
  }

  // ════════════════════════════════════════════════════════════════════════
  // GENERACIÓN MANUAL — daño o pérdida, registrada por el bibliotecario
  // ════════════════════════════════════════════════════════════════════════

  @Transactional
  public SancionResponseDTO registrarSancionManual(SancionManualRequestDTO request, Long bibliotecarioId) {
    // Verificar duplicado por préstamo si se especifica uno
    if (request.prestamoId() != null &&
        sancionRepository.existsByPrestamo_IdPrestamoAndEstado(request.prestamoId(), EstadoSancion.ACTIVA)) {
      throw new SancionDuplicadaException(request.prestamoId());
    }

    Sancion sancion = new Sancion();
    sancion.setUsuario(usuarioRepository.findById(request.usuarioId())
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + request.usuarioId())));
    sancion.setBiblioteca(bibliotecaRepository.findById(request.bibliotecaId())
        .orElseThrow(() -> new RuntimeException("Biblioteca no encontrada: " + request.bibliotecaId())));
    sancion.setBibliotecario(usuarioRepository.findById(bibliotecarioId)
        .orElseThrow(() -> new RuntimeException("Bibliotecario no encontrado: " + bibliotecarioId)));

    if (request.prestamoId() != null) {
      sancion.setPrestamo(prestamoRepository.findById(request.prestamoId())
          .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + request.prestamoId())));
    }

    sancion.setMotivo(request.motivo());
    sancion.setTipoSancion(TipoSancion.MULTA);
    sancion.setMontoMulta(request.montoFijo() != null ? request.montoFijo() : BigDecimal.ZERO);
    sancion.setObservaciones(request.observaciones());

    Sancion guardada = sancionRepository.save(sancion);
    log.info("Sanción manual creada [id={}] por bibliotecario [id={}]",
        guardada.getIdSancion(), bibliotecarioId);

    enviarNotificacionSancion(guardada);
    return mapper.toResponseDTO(guardada);
  }

  // ════════════════════════════════════════════════════════════════════════
  // CONSULTAS
  // ════════════════════════════════════════════════════════════════════════

  public SancionResponseDTO buscarPorId(Long idSancion) {
    return mapper.toResponseDTO(obtenerSancionOException(idSancion));
  }

  public List<SancionResponseDTO> historialPorUsuario(Long usuarioId) {
    return mapper.toResponseDTOList(sancionRepository.findHistorialByUsuario(usuarioId));
  }

  public List<SancionResponseDTO> sancionesActivasPorUsuario(Long usuarioId) {
    return mapper.toResponseDTOList(sancionRepository.findActivasByUsuario(usuarioId));
  }

  public List<SancionResponseDTO> sancionesPorBiblioteca(Long bibliotecaId, EstadoSancion estado) {
    return mapper.toResponseDTOList(
        sancionRepository.findByBibliotecaAndEstado(bibliotecaId, estado));
  }

  /**
   * Estado consolidado de sanciones de un usuario.
   * Módulo de préstamos y reservas consulta esto antes de permitir operaciones.
   */
  public EstadoSancionUsuarioDTO obtenerEstadoSanciones(Long usuarioId) {
    boolean tieneSuspension = sancionRepository.tieneSuspensionVigente(usuarioId);
    boolean tieneDeuda = sancionRepository.tieneDeudaPendiente(usuarioId);
    long totalActivas = sancionRepository.contarSancionesActivasPorUsuario(usuarioId);

    // Fecha de fin de suspensión más próxima (si tiene suspensión activa)
    LocalDate fechaFinProxima = sancionRepository.findActivasByUsuario(usuarioId)
        .stream()
        .filter(Sancion::implicaSuspension)
        .filter(s -> s.getFechaFinSuspension() != null)
        .map(Sancion::getFechaFinSuspension)
        .min(LocalDate::compareTo)
        .orElse(null);

    return new EstadoSancionUsuarioDTO(
        usuarioId, tieneSuspension, tieneDeuda, totalActivas, fechaFinProxima);
  }

  // ════════════════════════════════════════════════════════════════════════
  // GESTIÓN DE ESTADO — pago y condonación
  // ════════════════════════════════════════════════════════════════════════

  @Transactional
  public SancionResponseDTO registrarPago(Long idSancion, PagoMultaRequestDTO request) {
    Sancion sancion = obtenerSancionOException(idSancion);

    if (!sancion.estaActiva()) {
      throw new SancionNoActivaException(idSancion, sancion.getEstado().name());
    }

    // Si tiene suspensión activa, el pago solo salda la multa.
    // La suspensión sigue su curso hasta fechaFinSuspension.
    if (sancion.getTipoSancion() == TipoSancion.MULTA_Y_SUSPENSION
        && sancion.suspensionVigente()) {
      // Solo marcamos el monto como pagado, la sanción sigue ACTIVA por la suspensión
      sancion.setMontoMulta(BigDecimal.ZERO);
      sancion.setFechaPago(LocalDateTime.now());
      sancion.setMetodoPago(request.metodoPago());
    } else {
      sancion.setEstado(EstadoSancion.PAGADA);
      sancion.setFechaPago(LocalDateTime.now());
      sancion.setMetodoPago(request.metodoPago());
      if (request.observaciones() != null) {
        sancion.setObservaciones(request.observaciones());
      }
    }

    log.info("Pago registrado para sanción [id={}], método: {}", idSancion, request.metodoPago());
    return mapper.toResponseDTO(sancionRepository.save(sancion));
  }

  @Transactional
  public SancionResponseDTO condonar(Long idSancion, CondonacionRequestDTO request) {
    Sancion sancion = obtenerSancionOException(idSancion);

    if (!sancion.estaActiva()) {
      throw new SancionNoActivaException(idSancion, sancion.getEstado().name());
    }

    sancion.setEstado(EstadoSancion.CONDONADA);
    sancion.setFechaCondonacion(LocalDateTime.now());
    sancion.setObservaciones(request.observaciones());

    log.info("Sanción [id={}] condonada. Motivo: {}", idSancion, request.observaciones());
    return mapper.toResponseDTO(sancionRepository.save(sancion));
  }

  // ════════════════════════════════════════════════════════════════════════
  // CRON — procesar préstamos vencidos automáticamente
  // ════════════════════════════════════════════════════════════════════════

  /**
   * Se ejecuta cada día a las 2:00 AM.
   * Detecta préstamos vencidos sin sanción y los procesa.
   * Idempotente: la verificación de duplicados garantiza que ejecutarlo
   * dos veces produce el mismo resultado.
   */
  @Scheduled(cron = "0 0 2 * * *")
  @Transactional
  public void procesarPrestamosVencidos() {
    log.info("Iniciando CRON: procesarPrestamosVencidos");

    List<Prestamo> vencidos = prestamoRepository.findPrestamosVencidosSinSancion();
    log.info("Préstamos vencidos sin sanción encontrados: {}", vencidos.size());

    for (Prestamo prestamo : vencidos) {
      try {
        procesarDevolucionTardia(prestamo.getIdPrestamo());
      } catch (SancionDuplicadaException e) {
        // Idempotencia: ya fue procesado en una ejecución anterior
        log.warn("Préstamo [id={}] ya tiene sanción activa, se omite.", prestamo.getIdPrestamo());
      } catch (Exception e) {
        // Loguear pero continuar con el siguiente — no queremos que un error
        // detenga el procesamiento de todos los demás préstamos
        log.error("Error al procesar préstamo vencido [id={}]: {}",
            prestamo.getIdPrestamo(), e.getMessage(), e);
      }
    }

    log.info("CRON finalizado: procesarPrestamosVencidos");
  }

  /**
   * Cron diario a las 3:00 AM.
   * Cierra suspensiones que vencieron y no tienen deuda pendiente.
   */
  @Scheduled(cron = "0 0 3 * * *")
  @Transactional
  public void cerrarSuspensionesVencidas() {
    List<Sancion> vencidas = sancionRepository.findSuspensionesVencidasSinDeuda();
    for (Sancion s : vencidas) {
      s.setEstado(EstadoSancion.PAGADA);
      sancionRepository.save(s);
      log.info("Suspensión [id={}] cerrada automáticamente por vencimiento.", s.getIdSancion());
    }
  }

  // ════════════════════════════════════════════════════════════════════════
  // MÉTODOS PRIVADOS DE APOYO
  // ════════════════════════════════════════════════════════════════════════

  private int calcularDiasRetraso(Prestamo prestamo) {
    LocalDate fechaDevolucionReal = prestamo.getFechaDevolucionReal() != null
        ? prestamo.getFechaDevolucionReal().toLocalDate()
        : LocalDate.now();

    return (int) ChronoUnit.DAYS.between(
        prestamo.getFechaDevolucionEstimada(),
        fechaDevolucionReal);
  }

  private Sancion construirSancionPorRetraso(
      Prestamo prestamo,
      int diasRetraso,
      BigDecimal montoMulta,
      int diasSuspension,
      boolean debeSuspender) {
    Sancion sancion = new Sancion();
    sancion.setUsuario(prestamo.getUsuario());
    sancion.setPrestamo(prestamo);
    sancion.setBiblioteca(prestamo.getBiblioteca());
    sancion.setMotivo(MotivoSancion.RETRASO_DEVOLUCION);
    sancion.setDiasRetraso(diasRetraso);
    sancion.setMontoMulta(montoMulta);
    sancion.setIdConfigUsado(prestamo.getIdConfigUsado());

    if (debeSuspender) {
      sancion.setTipoSancion(TipoSancion.MULTA_Y_SUSPENSION);
      sancion.setDiasSuspension(diasSuspension);
      sancion.setFechaInicioSuspension(LocalDate.now());
      sancion.setFechaFinSuspension(LocalDate.now().plusDays(diasSuspension));
    } else {
      sancion.setTipoSancion(TipoSancion.MULTA);
    }

    return sancion;
  }

  private Long obtenerRolIdDelUsuario(Usuario usuario) {
    // Toma el primer rol del usuario para resolver la configuración
    return usuario.getRoles().stream()
        .findFirst()
        .map(rol -> rol.getId_role())
        .orElse(null);
  }

  private Sancion obtenerSancionOException(Long idSancion) {
    return sancionRepository.findById(idSancion)
        .orElseThrow(() -> new SancionNotFoundException(idSancion));
  }

  private void enviarNotificacionSancion(Sancion sancion) {
    try {
      // notificacionService.enviarNotificacionSancion(
      // sancion.getUsuario().getIdUsuario(),
      // sancion.getIdSancion(),
      // sancion.getMontoMulta(),
      // sancion.getFechaFinSuspension());
    } catch (Exception e) {
      // La notificación no es crítica — no revierte la sanción
      log.warn("No se pudo enviar notificación para sanción [id={}]: {}",
          sancion.getIdSancion(), e.getMessage());
    }
  }
}