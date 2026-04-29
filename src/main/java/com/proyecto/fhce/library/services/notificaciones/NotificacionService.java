package com.proyecto.fhce.library.services.notificaciones;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.proyecto.fhce.library.dto.request.CrearNotificacionRequest;
import com.proyecto.fhce.library.dto.response.ContadorNoLeidasResponse;
import com.proyecto.fhce.library.dto.response.NotificacionResponse;

import java.util.List;

public interface NotificacionService {

  /**
   * Crea y persiste una notificación con estado PENDIENTE.
   * Valida que no exista un duplicado activo antes de insertar.
   * El envío ocurre de forma asíncrona o mediante el scheduler.
   */
  NotificacionResponse crear(CrearNotificacionRequest request);

  /**
   * Devuelve el listado paginado de notificaciones del usuario autenticado.
   */
  Page<NotificacionResponse> listarPorUsuario(Long idUsuario, Pageable pageable);

  /**
   * Notificaciones no leídas del usuario autenticado.
   */
  List<NotificacionResponse> listarNoLeidas(Long idUsuario);

  /**
   * Cantidad de notificaciones no leídas (para el badge del menú).
   */
  ContadorNoLeidasResponse contarNoLeidas(Long idUsuario);

  /**
   * Marca una notificación específica como leída.
   * Valida que la notificación pertenezca al usuario autenticado (IDOR check).
   */
  NotificacionResponse marcarComoLeida(Long idNotificacion, Long idUsuario);

  /**
   * Marca todas las notificaciones no leídas del usuario como leídas.
   */
  int marcarTodasComoLeidas(Long idUsuario);

  /**
   * Procesa las notificaciones PENDIENTE y reintenta las FALLIDO.
   * Llamado por el scheduler — no expuesto en la API pública.
   */
  void procesarPendientes();

  /**
   * Reintenta el envío de notificaciones FALLIDO que no superaron el límite de
   * intentos.
   */
  void procesarReintentos();
}