package com.proyecto.fhce.library.services.notificaciones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.CrearNotificacionRequest;
import com.proyecto.fhce.library.dto.response.ContadorNoLeidasResponse;
import com.proyecto.fhce.library.dto.response.NotificacionResponse;
import com.proyecto.fhce.library.entities.Notificacion;
import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;
import com.proyecto.fhce.library.enums.notificaciones.EstadoEnvio;
import com.proyecto.fhce.library.exception.NotificacionException;
import com.proyecto.fhce.library.mapper.NotificacionMapper;
import com.proyecto.fhce.library.repositories.NotificacionRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class NotificacionServiceImpl implements NotificacionService {

  private static final Logger log = LoggerFactory.getLogger(NotificacionServiceImpl.class);
  private static final int MAX_INTENTOS_ENVIO = 3;

  private final NotificacionRepository notificacionRepository;
  private final NotificacionMapper mapper;

  // Spring inyecta todas las implementaciones de CanalEnvioService.
  // El mapa permite resolver el adaptador correcto por canal sin if/else.
  private final Map<CanalNotificacion, CanalEnvioService> canales;

  public NotificacionServiceImpl(
      NotificacionRepository notificacionRepository,
      NotificacionMapper mapper,
      List<CanalEnvioService> listaCanales) {
    this.notificacionRepository = notificacionRepository;
    this.mapper = mapper;
    this.canales = listaCanales.stream()
        .collect(Collectors.toMap(CanalEnvioService::canal, Function.identity()));
  }

  // -------------------------------------------------------
  // CREACIÓN
  // -------------------------------------------------------

  @Override
  @Transactional
  public NotificacionResponse crear(CrearNotificacionRequest request) {

    if (request.idReferencia() != null) {
      boolean duplicada = notificacionRepository.existeNotificacionActiva(
          request.idUsuario(),
          request.tipoNotificacion(),
          request.idReferencia());
      if (duplicada) {
        throw NotificacionException.duplicada(
            request.idUsuario(),
            request.tipoNotificacion().name());
      }
    }

    CanalNotificacion canal = request.canal() != null
        ? request.canal()
        : CanalNotificacion.SISTEMA;

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
    log.info("Notificación creada: id={} tipo={} usuario={}",
        guardada.getIdNotificacion(),
        guardada.getTipoNotificacion(),
        guardada.getIdUsuario());

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
  // -------------------------------------------------------

  @Override
  @Transactional
  public void procesarPendientes() {
    List<Notificacion> pendientes = notificacionRepository.findByEstadoEnvio(EstadoEnvio.PENDIENTE);
    if (pendientes.isEmpty())
      return;

    log.info("Procesando {} notificaciones pendientes", pendientes.size());
    pendientes.forEach(this::intentarEnvio);
  }

  @Override
  @Transactional
  public void procesarReintentos() {
    List<Notificacion> reintentos = notificacionRepository.findReintentos(MAX_INTENTOS_ENVIO);
    if (reintentos.isEmpty())
      return;

    log.info("Reintentando envío de {} notificaciones fallidas", reintentos.size());
    reintentos.forEach(this::intentarEnvio);
  }

  // -------------------------------------------------------
  // Lógica privada de envío
  // -------------------------------------------------------

  private void intentarEnvio(Notificacion notificacion) {
    CanalEnvioService canalService = canales.get(notificacion.getCanal());

    if (canalService == null) {
      log.error("Canal desconocido: {} para notificación {}",
          notificacion.getCanal(), notificacion.getIdNotificacion());
      notificacion.registrarFallo("Canal no configurado: " + notificacion.getCanal());
      notificacionRepository.save(notificacion);
      return;
    }

    if (!canalService.estaDisponible()) {
      log.warn("Canal {} no disponible para notificación {}",
          notificacion.getCanal(), notificacion.getIdNotificacion());
      notificacion.registrarFallo("Canal no disponible: " + notificacion.getCanal());
      notificacionRepository.save(notificacion);
      return;
    }

    try {
      canalService.enviar(notificacion);
      notificacion.registrarEnvioExitoso();
      log.info("Notificación {} enviada correctamente por {}",
          notificacion.getIdNotificacion(), notificacion.getCanal());
    } catch (Exception e) {
      notificacion.registrarFallo(e.getMessage());
      log.warn("Fallo al enviar notificación {} (intento {}): {}",
          notificacion.getIdNotificacion(),
          notificacion.getIntentosEnvio(),
          e.getMessage());
    }

    notificacionRepository.save(notificacion);
  }
}