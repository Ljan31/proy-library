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
import com.proyecto.fhce.library.dto.SancionDTO.SancionResumenDTO;
import com.proyecto.fhce.library.dto.request.CrearNotificacionRequest;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionPrestamoResponseDTO;
import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.MotivoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.enums.notificaciones.TipoNotificacion;
import com.proyecto.fhce.library.exception.SancionException.PrestamoNoVencidoException;
import com.proyecto.fhce.library.exception.SancionException.SancionDuplicadaException;
import com.proyecto.fhce.library.exception.SancionException.SancionNoActivaException;
import com.proyecto.fhce.library.exception.SancionException.SancionNotFoundException;
import com.proyecto.fhce.library.mapper.SancionMapper;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.SancionRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.notificaciones.NotificacionService;

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
  private final NotificacionService notificacionService;
  private final SancionMapper mapper;

  public SancionService(
      SancionRepository sancionRepository,
      PrestamoRepository prestamoRepository,
      UserRepository usuarioRepository,
      BibliotecaRepository bibliotecaRepository,
      ConfiguracionPrestamoService configuracionService,
      NotificacionService notificacionService,
      SancionMapper mapper) {
    this.sancionRepository = sancionRepository;
    this.prestamoRepository = prestamoRepository;
    this.usuarioRepository = usuarioRepository;
    this.bibliotecaRepository = bibliotecaRepository;
    this.configuracionService = configuracionService;
    this.notificacionService = notificacionService;
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
    log.info("==================================================");
    log.info("INICIANDO procesamiento de devolución tardía");
    log.info("ID préstamo recibido: {}", idPrestamo);
    Prestamo prestamo = prestamoRepository.findByIdWithRelations(idPrestamo)
        .orElseThrow(() -> new RuntimeException("Préstamo no encontrado: " + idPrestamo));

    // ① Verificar que no exista sanción activa ya creada para este préstamo
    if (sancionRepository.existsByPrestamo_IdPrestamoAndEstado(idPrestamo, EstadoSancion.ACTIVA)) {
      log.warn("Ya existe una sanción activa para el préstamo ID={}",
          idPrestamo);
      throw new SancionDuplicadaException(idPrestamo);
    }

    // ② Calcular días de retraso
    // Si ya fue devuelto, usamos la fecha real; si no, usamos hoy
    LocalDate fechaDevolucionReal = prestamo.getFechaDevolucionReal() != null
        ? prestamo.getFechaDevolucionReal().toLocalDate()
        : null;

    // ② Calcular días de retraso
    int diasRetraso = CalculadoraMultas.calcularDiasRetraso(
        prestamo.getFechaDevolucionEstimada(),
        fechaDevolucionReal);
    // int diasRetraso = calcularDiasRetraso(prestamo);
    if (diasRetraso <= 0) {
      log.warn("El préstamo aún no está vencido. ID={}", idPrestamo);

      throw new PrestamoNoVencidoException(idPrestamo);
    }

    // ③ Obtener la configuración histórica del préstamo
    // SIEMPRE se usa idConfigUsado guardado en el préstamo,
    // aunque la config de la biblioteca haya cambiado después
    ConfiguracionPrestamo config = obtenerConfigHistorica(prestamo.getIdConfigUsado(), idPrestamo);
    // ③ Obtener monto de multa desde ConfiguracionPrestamoService
    // Usamos el idConfig guardado en el préstamo para respetar la config histórica
    // BigDecimal montoMulta = configuracionService.calcularMulta(
    // prestamo.getBiblioteca().getIdBiblioteca(), diasRetraso);
    BigDecimal montoMulta = CalculadoraMultas.calcularMonto(diasRetraso, config);

    log.info("Monto multa calculado: {}", montoMulta);

    // ④ Determinar si corresponde suspensión
    // boolean debeSuspender = configuracionService.debeSuspender(
    // prestamo.getBiblioteca().getIdBiblioteca(), diasRetraso);
    boolean debeSuspender = CalculadoraMultas.debeSuspender(diasRetraso, config);

    log.info("¿Debe suspender?: {}", debeSuspender);

    // ⑤ Obtener configuración completa si hay suspensión (para los días)
    int diasSuspension = 0;
    if (debeSuspender) {
      // ConfiguracionPrestamoResponseDTO config = configuracionService
      // .buscarPorId(prestamo.getIdConfigUsado());

      // int diasSuspension = debeSuspender ? config.getDiasSuspension() : 0;
      log.info("Días de suspensión obtenidos: {}",
          diasSuspension);
      diasSuspension = config.getDiasSuspension();
    }

    // ⑥ Construir y persistir la sanción
    Sancion sancion = construirSancionPorRetraso(
        prestamo, diasRetraso, montoMulta, diasSuspension, debeSuspender);
    // Sancion sancion = construirSancionPorRetraso(
    // prestamo, diasRetraso, montoMulta, config, debeSuspender);
    Sancion guardada = sancionRepository.save(sancion);

    // log.info("Sanción creada [id={}] para préstamo [id={}], usuario [id={}],
    // diasRetraso={}",
    // guardada.getIdSancion(), idPrestamo, prestamo.getUsuario().getId_usuario(),
    // diasRetraso);
    log.info(
        "Resumen -> [id={}], préstamo={}, usuario={}, retraso={} días, multa={}, suspensión={} días",
        guardada.getIdSancion(),
        idPrestamo,
        prestamo.getUsuario().getId_usuario(),
        diasRetraso,
        montoMulta,
        diasSuspension);

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
    List<Sancion> sancionesActivas = sancionRepository.findActivasByUsuario(usuarioId);
    // Fecha de fin de suspensión más próxima (si tiene suspensión activa)
    LocalDate fechaFinProxima = sancionRepository.findActivasByUsuario(usuarioId)
        .stream()
        .filter(Sancion::implicaSuspension)
        .filter(s -> s.getFechaFinSuspension() != null)
        .map(Sancion::getFechaFinSuspension)
        .min(LocalDate::compareTo)
        .orElse(null);

    List<SancionResumenDTO> sancionesResumen = sancionesActivas.stream()
        .map(s -> new SancionResumenDTO(
            s.getIdSancion(),
            s.getPrestamo() != null
                ? s.getPrestamo().getIdPrestamo()
                : null,
            s.getTipoSancion(),
            s.getEstado(),
            s.getMontoMulta(),
            s.getFechaFinSuspension(),
            s.implicaSuspension()
                && s.getFechaFinSuspension() != null
                && !s.getFechaFinSuspension().isBefore(LocalDate.now()),
            s.getMotivo() != null
                ? s.getMotivo().name()
                : null))
        .toList();
    BigDecimal montoTotalDeuda = sancionesResumen.stream()
        .map(SancionResumenDTO::montoMulta)
        .filter(monto -> monto != null)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return new EstadoSancionUsuarioDTO(
        usuarioId,
        tieneSuspension,
        tieneDeuda,
        totalActivas,
        montoTotalDeuda,
        fechaFinProxima,
        sancionesResumen);
  }

  /**
   * Calcula el monto que el usuario pagaría si abona HOY.
   * No modifica ningún dato — es solo una consulta informativa.
   *
   * Si la multa está congelada, retorna el tope máximo.
   * Si la sanción no es de tipo MULTA (o es manual sin config), retorna el
   * montoMulta guardado.
   */
  public BigDecimal calcularMontoActual(Long idSancion) {
    Sancion sancion = obtenerSancionOException(idSancion);

    if (!sancion.estaActiva()) {
      return sancion.getMontoMulta();
    }

    // Solo recalculamos para sanciones por retraso que tienen config histórica
    if (sancion.getIdConfigUsado() == null || sancion.getPrestamo() == null) {
      return sancion.getMontoMulta();
    }

    ConfiguracionPrestamo config = configuracionService
        .obtenerEntidadPorId(sancion.getIdConfigUsado());

    if (config == null) {
      return sancion.getMontoMulta();
    }

    return CalculadoraMultas.calcularMontoActual(
        sancion.getPrestamo().getFechaDevolucionEstimada(),
        config);
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
  @Scheduled(cron = "0 30 2 * * *")
  @Transactional
  public void procesarPrestamosVencidos() {
    log.info("==================================================");

    log.info("Iniciando CRON: procesarPrestamosVencidos");
    log.info("Fecha/Hora ejecución: {}", LocalDateTime.now());

    List<Prestamo> vencidos = prestamoRepository.findPrestamosVencidosSinSancion();
    log.info("Préstamos vencidos sin sanción encontrados: {}", vencidos.size());
    if (vencidos.isEmpty()) {
      log.info("No existen préstamos pendientes por procesar.");
    }
    int procesados = 0, errores = 0;
    for (Prestamo prestamo : vencidos) {
      log.info("--------------------------------------------------");
      log.info("Procesando préstamo ID: {}", prestamo.getIdPrestamo());

      try {
        // Logs de seguimiento del flujo
        log.info("Usuario: {}",
            prestamo.getUsuario() != null
                ? prestamo.getUsuario().getUsername()
                : "SIN USUARIO");

        log.info("Fecha préstamo: {}", prestamo.getFechaPrestamo());
        log.info("Fecha devolución esperada: {}", prestamo.getFechaDevolucionEstimada());

        if (prestamo.getEjemplar() != null) {
          log.info("Ejemplar ID: {}", prestamo.getEjemplar().getIdEjemplar());

          if (prestamo.getEjemplar().getEdicion().getLibro() != null) {
            log.info("Libro: {}",
                prestamo.getEjemplar().getEdicion().getLibro().getTitulo());
          }
        }

        log.info("Invocando procesarDevolucionTardia({})...",
            prestamo.getIdPrestamo());
        procesarDevolucionTardia(prestamo.getIdPrestamo());
        log.info("Préstamo procesado correctamente [id={}]",
            prestamo.getIdPrestamo());
        procesados++;
      } catch (SancionDuplicadaException e) {
        // Idempotencia: ya fue procesado en una ejecución anterior
        log.warn("Préstamo [id={}] ya tiene sanción activa, se omite.", prestamo.getIdPrestamo());
      } catch (Exception e) {
        errores++;
        // Loguear pero continuar con el siguiente — no queremos que un error
        // detenga el procesamiento de todos los demás préstamos
        log.error("Error al procesar préstamo vencido [id={}]: {}",
            prestamo.getIdPrestamo(), e.getMessage(), e);
      }
    }

    log.info("CRON finalizado: procesarPrestamosVencidos");
    log.info("CRON fin — procesarPrestamosVencidos: {} procesados, {} errores", procesados, errores);

    log.info("==================================================");

  }

  /**
   * CRON 2 — 02:30 AM todos los días.
   *
   * Actualiza el montoMulta de sanciones ACTIVAS por retraso que aún no
   * alcanzaron el tope. Una vez que la multa está congelada, no se toca más.
   *
   * Ejemplo con multaMaxDias=7, multaPorDia=3:
   * Día 1 → guardado Bs 3 → CRON día 2 lo actualiza a Bs 6
   * Día 2 → guardado Bs 6 → CRON día 3 lo actualiza a Bs 9
   * ...
   * Día 7 → guardado Bs 21 → CONGELADO — CRON ya no toca este registro
   *
   * Si el usuario paga en el día 4 (Bs 12), el CRON no lo toca porque
   * el estado ya no es ACTIVA.
   */
  @Scheduled(cron = "0 30 2 * * *")
  @Transactional
  public void actualizarMultasSanciones() {
    log.info("CRON inicio — actualizarMultasSanciones");

    // Solo sanciones activas, por retraso, con config histórica guardada
    List<Sancion> candidatas = sancionRepository.findActivasPorRetrasoConConfig();

    int actualizadas = 0, congeladas = 0, errores = 0;

    for (Sancion sancion : candidatas) {
      try {
        boolean cambio = actualizarMontoSancion(sancion);
        if (cambio)
          actualizadas++;
        else
          congeladas++;
      } catch (Exception e) {
        errores++;
        log.error("Error actualizando sanción [id={}]: {}", sancion.getIdSancion(), e.getMessage(), e);
      }
    }

    log.info("CRON fin — actualizarMultasSanciones: {} actualizadas, {} congeladas, {} errores",
        actualizadas, congeladas, errores);
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

  /**
   * Recalcula y persiste el monto de una sanción activa.
   *
   * Retorna true si el monto cambió (sanción actualizada).
   * Retorna false si la multa ya estaba congelada (sin cambios).
   *
   * El método también activa la suspensión si el retraso superó multaMaxDias
   * y la sanción aún era de tipo MULTA.
   */
  private boolean actualizarMontoSancion(Sancion sancion) {
    ConfiguracionPrestamo config = configuracionService
        .obtenerEntidadPorId(sancion.getIdConfigUsado());
    // ConfiguracionPrestamo config = configuracionRepository
    // .findById(sancion.getIdConfigUsado())
    // .orElse(null);

    if (config == null) {
      log.warn("Config [id={}] no encontrada para sanción [id={}]. Se omite.",
          sancion.getIdConfigUsado(), sancion.getIdSancion());
      return false;
    }

    // Calcular días de retraso actuales (el préstamo sigue activo → usa hoy)
    LocalDate fechaEstimada = sancion.getPrestamo().getFechaDevolucionEstimada();
    int diasRetraso = CalculadoraMultas.calcularDiasRetraso(fechaEstimada, null);

    // Si la multa ya está congelada, no hay nada que actualizar
    if (CalculadoraMultas.multaCongelada(diasRetraso, config)) {
      BigDecimal montoMaximo = CalculadoraMultas.calcularMontoMaximo(config);

      // Garantizamos que el monto guardado sea exactamente el tope
      // (por si en alguna ejecución anterior quedó desincronizado)
      if (montoMaximo != null
          && sancion.getMontoMulta().compareTo(montoMaximo) != 0) {
        sancion.setMontoMulta(montoMaximo);
        sancion.setDiasRetraso(diasRetraso);
        sancionRepository.save(sancion);
      }
      return false; // ya congelada
    }

    // Calcular nuevo monto con los días actuales
    BigDecimal nuevoMonto = CalculadoraMultas.calcularMonto(diasRetraso, config);
    BigDecimal montoAnterior = sancion.getMontoMulta();

    // Actualizar solo si hubo cambio real (evita writes innecesarios)
    if (nuevoMonto.compareTo(montoAnterior) == 0) {
      return false;
    }

    sancion.setMontoMulta(nuevoMonto);
    sancion.setDiasRetraso(diasRetraso);
    String observacionAutomatica = String.format(
        "Multa actualizada automáticamente. Días de retraso: %d. " +
            "Monto acumulado: Bs %.2f.",
        diasRetraso,
        nuevoMonto);
    sancion.setObservaciones(observacionAutomatica);

    // ── Verificar si debe activarse suspensión ───────────────────────────
    // Puede ocurrir que en el CRON del día anterior se creó como MULTA
    // y hoy ya superó el tope → se convierte en MULTA_Y_SUSPENSION
    if (CalculadoraMultas.debeSuspender(diasRetraso, config)
        && sancion.getTipoSancion() == TipoSancion.MULTA) {

      sancion.setTipoSancion(TipoSancion.MULTA_Y_SUSPENSION);
      sancion.setDiasSuspension(config.getDiasSuspension());
      sancion.setFechaInicioSuspension(LocalDate.now());
      sancion.setFechaFinSuspension(LocalDate.now().plusDays(config.getDiasSuspension()));
      sancion.setObservaciones(
          sancion.getObservaciones()
              + " Se activó suspensión por exceder el límite permitido.");
      log.info("Sanción [id={}] escalada a MULTA_Y_SUSPENSION — diasRetraso={}, usuario [id={}]",
          sancion.getIdSancion(), diasRetraso, sancion.getUsuario().getId_usuario());

      // * fatal implementar notificacion */
      // enviarNotificacionSuspension(sancion);
    }

    sancionRepository.save(sancion);

    log.debug("Sanción [id={}] actualizada — monto: {} → {}, diasRetraso: {}",
        sancion.getIdSancion(), montoAnterior, nuevoMonto, diasRetraso);

    return true;
  }

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

  private ConfiguracionPrestamo obtenerConfigHistorica(Long idConfig, Long idPrestamo) {
    if (idConfig == null) {
      throw new RuntimeException(
          "El préstamo [id=" + idPrestamo + "] no tiene idConfigUsado guardado. " +
              "No se puede calcular la sanción sin la configuración histórica.");
    }
    return configuracionService.obtenerEntidadPorId(idConfig);
  }

  private void enviarNotificacionSancion(Sancion sancion) {
    try {

      String asunto = "Sanción por devolución tardía";

      String mensaje = String.format(
          "Se ha generado una sanción por devolución tardía.%n" +
              "Monto de multa: %s.%n" +
              "%s",
          sancion.getMontoMulta(),
          sancion.getFechaFinSuspension() != null
              ? "Suspensión hasta: " + sancion.getFechaFinSuspension()
              : "No se aplicó suspensión.");

      CrearNotificacionRequest request = new CrearNotificacionRequest(
          sancion.getUsuario().getId_usuario(),
          TipoNotificacion.SANCION,
          asunto,
          mensaje,
          null, //
          sancion.getIdSancion(),
          "SANCION");

      notificacionService.crear(request);
    } catch (Exception e) {
      // La notificación no es crítica — no revierte la sanción
      log.warn("No se pudo enviar notificación para sanción [id={}]: {}",
          sancion.getIdSancion(), e.getMessage());
    }
  }

  // private void enviarNotificacionSuspension(Sancion sancion) {
  // try {
  // notificacionService.enviarNotificacionSuspension(
  // sancion.getUsuario().getId_usuario(),
  // sancion.getIdSancion(),
  // sancion.getFechaFinSuspension());
  // } catch (Exception e) {
  // log.warn("No se pudo enviar notificación de suspensión para sanción [id={}]:
  // {}",
  // sancion.getIdSancion(), e.getMessage());
  // }
  // }
}