package com.proyecto.fhce.library.services.notificaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.CrearNotificacionExternaRequest;
import com.proyecto.fhce.library.dto.request.CrearNotificacionRequest;
import com.proyecto.fhce.library.dto.response.ContadorNoLeidasResponse;
import com.proyecto.fhce.library.dto.response.NotificacionResponse;
import com.proyecto.fhce.library.entities.Notificacion;
import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;
import com.proyecto.fhce.library.enums.notificaciones.EstadoEnvio;
import com.proyecto.fhce.library.exception.DestinatarioInvalidoException;
import com.proyecto.fhce.library.exception.NotificacionException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.mapper.NotificacionMapper;
import com.proyecto.fhce.library.repositories.NotificacionRepository;
import com.proyecto.fhce.library.repositories.SolicitudCertificadoRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificacionServiceImpl implements NotificacionService {

  private static final Logger log = LoggerFactory.getLogger(NotificacionServiceImpl.class);
  private static final int MAX_INTENTOS_ENVIO = 3;
  private static final int MAX_LARGO_ERROR = 1000;

  private final NotificacionRepository notificacionRepository;
  private final SolicitudCertificadoRepository solicitudRepository;
  private final NotificacionMapper mapper;

  // Spring inyecta todas las implementaciones de CanalEnvioService.
  // El mapa permite resolver el adaptador correcto por canal sin if/else.
  private final Map<CanalNotificacion, CanalEnvioService> canales;

  public NotificacionServiceImpl(
      NotificacionRepository notificacionRepository,
      NotificacionMapper mapper,
      List<CanalEnvioService> listaCanales, SolicitudCertificadoRepository solicitudRepository) {
    this.notificacionRepository = notificacionRepository;
    this.solicitudRepository = solicitudRepository;
    this.mapper = mapper;
    this.canales = listaCanales.stream()
        .collect(Collectors.toMap(CanalEnvioService::canal, Function.identity()));
  }

  // -------------------------------------------------------
  // CREACIÓN
  // -------------------------------------------------------

  // noRollbackFor: "duplicada" es un caso normal, no un error de datos. Si el
  // llamador ya está en una transacción (p. ej. al registrar una solicitud) y
  // captura esta excepción, no debe quedar marcada para rollback y hacer fallar
  // todo el commit con UnexpectedRollbackException.
  @Override
  @Transactional(noRollbackFor = NotificacionException.class)
  public NotificacionResponse crear(CrearNotificacionRequest request) {
    System.out.println("llega: " + request.canal());
    CanalNotificacion canal = request.canal() != null
        ? request.canal()
        : CanalNotificacion.SISTEMA;
    System.out.println("canalCrear: " + canal);
    // El canal es parte de la clave: un evento puede ir por SISTEMA y por EMAIL.
    if (request.idReferencia() != null) {
      boolean duplicada = notificacionRepository.existeNotificacionActiva(
          request.idUsuario(),
          request.tipoNotificacion(),
          canal,
          request.idReferencia());
      if (duplicada) {
        throw NotificacionException.duplicada(
            request.idUsuario(),
            request.tipoNotificacion().name());
      }
    }

    Notificacion notificacion = new Notificacion(
        request.idUsuario(),
        request.tipoNotificacion(),
        request.asunto(),
        request.mensaje(),
        EstadoEnvio.PENDIENTE,
        canal,
        request.idReferencia(),
        request.tipoReferencia());

    Notificacion guardada = notificacionRepository.save(notificacion);
    log.info("Notificación creada: id={} tipo={} canal={} usuario={}",
        guardada.getIdNotificacion(),
        guardada.getTipoNotificacion(),
        guardada.getCanal(),
        guardada.getIdUsuario());

    return mapper.toResponse(guardada);
  }

  @Override
  @Transactional(noRollbackFor = NotificacionException.class)
  public List<NotificacionResponse> crearEnCanales(CrearNotificacionRequest request,
      Set<CanalNotificacion> canalesSolicitados) {

    List<NotificacionResponse> creadas = new ArrayList<>();

    for (CanalNotificacion canal : canalesSolicitados) {
      System.out.println("crearEnCanales: " + canal);
      CanalEnvioService servicio = canales.get(canal);
      if (servicio == null || !servicio.estaDisponible()) {
        log.debug("Canal {} no disponible: se omite para usuario {}", canal, request.idUsuario());
        continue;
      }

      CrearNotificacionRequest porCanal = new CrearNotificacionRequest(
          request.idUsuario(),
          request.tipoNotificacion(),
          request.asunto(),
          request.mensaje(),
          canal,
          request.idReferencia(),
          request.tipoReferencia());

      try {
        creadas.add(crear(porCanal));
      } catch (NotificacionException e) {
        if (e.getStatus() != HttpStatus.CONFLICT) {
          throw e;
        }
        log.debug("Ya existía notificación {} por {} para usuario {}: se omite",
            request.tipoNotificacion(), canal, request.idUsuario());
      }
    }
    return creadas;
  }

  @Override
  @Transactional(noRollbackFor = NotificacionException.class)
  public NotificacionResponse crearYEnviarExterna(
      CrearNotificacionRequest request) {

    CanalNotificacion canal = CanalNotificacion.EMAIL;

    // Validar referencia
    if (request.idReferencia() == null ||
        !"SOLICITUD_CERTIFICADO".equals(request.tipoReferencia())) {
      throw new IllegalArgumentException(
          "Se requiere una referencia válida de solicitud de certificado");
    }

    // Obtener solicitud
    SolicitudCertificado solicitud = solicitudRepository
        .findById(request.idReferencia())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Solicitud de certificado no encontrada"));

    // Validar correo
    String email = Optional.ofNullable(solicitud.getEmail())
        .map(String::trim)
        .filter(valor -> !valor.isEmpty())
        .orElseThrow(() -> new IllegalArgumentException(
            "La solicitud no tiene un correo válido"));

    // Evitar duplicados por referencia
    boolean duplicada = notificacionRepository.existeNotificacionActivaPorReferencia(
        request.tipoNotificacion(),
        canal,
        request.idReferencia(),
        request.tipoReferencia());

    if (duplicada) {
      throw NotificacionException.duplicada(
          null,
          request.tipoNotificacion().name());
    }

    // Crear notificación sin usuario asociado
    Notificacion notificacion = new Notificacion(
        null,
        request.tipoNotificacion(),
        request.asunto(),
        request.mensaje(),
        EstadoEnvio.PENDIENTE,
        canal,
        request.idReferencia(),
        request.tipoReferencia());

    Notificacion guardada = notificacionRepository.save(notificacion);

    log.info(
        "Notificación externa creada: id={} referencia={} email={}",
        guardada.getIdNotificacion(),
        guardada.getIdReferencia(),
        email);

    // Intentar envío
    intentarEnvio(guardada);

    return mapper.toResponse(guardada);
  }
  // -------------------------------------------------------
  // CONSULTAS
  // -------------------------------------------------------

  @Override
  @Transactional(readOnly = true)
  public Page<NotificacionResponse> listarPorUsuario(Long idUsuario, Pageable pageable) {
    return notificacionRepository
        .findByIdUsuarioOrderByFechaCreacionDesc(idUsuario, pageable)
        .map(mapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public List<NotificacionResponse> listarNoLeidas(Long idUsuario) {
    return notificacionRepository.findNoLeidasByUsuario(idUsuario)
        .stream()
        .map(mapper::toResponse)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ContadorNoLeidasResponse contarNoLeidas(Long idUsuario) {
    long total = notificacionRepository.countNoLeidasByUsuario(idUsuario);
    return new ContadorNoLeidasResponse(total);
  }

  // -------------------------------------------------------
  // LECTURA
  // -------------------------------------------------------

  @Override
  @Transactional
  public NotificacionResponse marcarComoLeida(Long idNotificacion, Long idUsuario) {
    Notificacion notificacion = notificacionRepository
        .findByIdNotificacionAndIdUsuario(idNotificacion, idUsuario)
        .orElseThrow(() -> NotificacionException.noEncontrada(idNotificacion));

    notificacion.marcarComoLeida();
    return mapper.toResponse(notificacionRepository.save(notificacion));
  }

  @Override
  @Transactional
  public int marcarTodasComoLeidas(Long idUsuario) {
    int actualizadas = notificacionRepository.marcarTodasComoLeidas(idUsuario, LocalDateTime.now());
    log.info("{} notificaciones marcadas como leídas para usuario {}", actualizadas, idUsuario);
    return actualizadas;
  }

  // -------------------------------------------------------
  // PROCESAMIENTO (scheduler)
  //
  // Estos métodos NO son @Transactional a propósito: hablar con un servidor
  // SMTP puede tardar segundos y no debe mantener una transacción (ni una
  // conexión a la BD) abierta. Cada notificación guarda su resultado en su
  // propia transacción corta, justo después de enviarse.
  // -------------------------------------------------------

  @Override
  public void procesarPendientes() {
    List<Notificacion> pendientes = notificacionRepository.findByEstadoEnvio(EstadoEnvio.PENDIENTE);
    if (pendientes.isEmpty())
      return;

    log.info("Procesando {} notificaciones pendientes", pendientes.size());
    procesarLote(pendientes);
  }

  @Override
  public void procesarReintentos() {
    List<Notificacion> reintentos = notificacionRepository.findReintentos(MAX_INTENTOS_ENVIO);
    if (reintentos.isEmpty())
      return;

    log.info("Reintentando envío de {} notificaciones fallidas", reintentos.size());
    procesarLote(reintentos);
  }

  // -------------------------------------------------------
  // Lógica privada de envío
  // -------------------------------------------------------

  private void procesarLote(List<Notificacion> lote) {
    Map<CanalNotificacion, Integer> cupos = calcularCupos();
    Set<CanalNotificacion> sinCupoAvisado = EnumSet.noneOf(CanalNotificacion.class);

    for (Notificacion notificacion : lote) {
      CanalNotificacion canal = notificacion.getCanal();
      Integer cupo = cupos.get(canal);

      if (cupo != null && cupo <= 0) {
        // Cupo diario agotado: se deja como está (sin gastar intentos)
        // y se reanuda solo cuando la ventana de 24 h libere cupo.
        if (sinCupoAvisado.add(canal)) {
          log.warn("Cupo diario agotado para el canal {}: las notificaciones esperan", canal);
        }
        continue;
      }

      boolean enviada = intentarEnvio(notificacion);
      if (enviada && cupo != null) {
        cupos.put(canal, cupo - 1);
      }
    }
  }

  /** Cupo restante por canal (solo canales con límite) en las últimas 24 h. */
  private Map<CanalNotificacion, Integer> calcularCupos() {
    Map<CanalNotificacion, Integer> cupos = new EnumMap<>(CanalNotificacion.class);
    LocalDateTime desde = LocalDateTime.now().minusHours(24);

    for (Map.Entry<CanalNotificacion, CanalEnvioService> entrada : canales.entrySet()) {
      int limite = entrada.getValue().limiteDiario();
      if (limite > 0) {
        long enviadas = notificacionRepository.countByCanalAndEstadoEnvioAndFechaEnvioAfter(
            entrada.getKey(), EstadoEnvio.ENVIADO, desde);
        cupos.put(entrada.getKey(), (int) Math.max(0, limite - enviadas));
      }
    }
    return cupos;
  }

  /** @return true si la notificación quedó ENVIADO. */
  private boolean intentarEnvio(Notificacion notificacion) {
    CanalEnvioService canalService = canales.get(notificacion.getCanal());

    if (canalService == null) {
      log.error("Canal desconocido: {} para notificación {}",
          notificacion.getCanal(), notificacion.getIdNotificacion());
      notificacion.registrarFallo("Canal no configurado: " + notificacion.getCanal());
      guardarResultado(notificacion);
      return false;
    }

    if (!canalService.estaDisponible()) {
      log.warn("Canal {} no disponible para notificación {}",
          notificacion.getCanal(), notificacion.getIdNotificacion());
      notificacion.registrarFallo("Canal no disponible: " + notificacion.getCanal());
      guardarResultado(notificacion);
      return false;
    }

    try {
      canalService.enviar(notificacion);
      notificacion.registrarEnvioExitoso();
      log.info("Notificación {} enviada correctamente por {}",
          notificacion.getIdNotificacion(), notificacion.getCanal());
    } catch (DestinatarioInvalidoException e) {
      // Permanente: reintentar no sirve (el usuario no tiene correo/teléfono válido)
      notificacion.registrarFalloDefinitivo(recortar(e.getMessage()), MAX_INTENTOS_ENVIO);
      log.warn("Notificación {} no se reintentará: {}", notificacion.getIdNotificacion(), e.getMessage());
    } catch (Exception e) {
      notificacion.registrarFallo(recortar(e.getMessage()));
      log.warn("Fallo al enviar notificación {} (intento {}): {}",
          notificacion.getIdNotificacion(),
          notificacion.getIntentosEnvio(),
          e.getMessage());
    }

    // Fuera del try: si falla la BD aquí, no se confunde con un fallo de envío.
    guardarResultado(notificacion);
    return notificacion.getEstadoEnvio() == EstadoEnvio.ENVIADO;
  }

  private void guardarResultado(Notificacion notificacion) {
    notificacionRepository.actualizarResultadoEnvio(
        notificacion.getIdNotificacion(),
        notificacion.getEstadoEnvio(),
        notificacion.getFechaEnvio(),
        notificacion.getIntentosEnvio(),
        notificacion.getUltimoError());
  }

  private static String recortar(String mensaje) {
    if (mensaje == null)
      return "Error desconocido";
    return mensaje.length() > MAX_LARGO_ERROR ? mensaje.substring(0, MAX_LARGO_ERROR) : mensaje;
  }
}
