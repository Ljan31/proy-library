package com.proyecto.fhce.library.services.notificaciones;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.proyecto.fhce.library.dto.request.CrearNotificacionExternaRequest;
import com.proyecto.fhce.library.dto.request.CrearNotificacionRequest;
import com.proyecto.fhce.library.dto.response.ContadorNoLeidasResponse;
import com.proyecto.fhce.library.dto.response.NotificacionResponse;
import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;

import java.util.List;
import java.util.Set;

public interface NotificacionService {

  /**
   * Crea y persiste una notificación con estado PENDIENTE.
   * Valida que no exista un duplicado activo antes de insertar.
   * El envío ocurre de forma asíncrona o mediante el scheduler.
   */
  NotificacionResponse crear(CrearNotificacionRequest request);

  /**
   * Crea la misma notificación en varios canales (p. ej. SISTEMA + EMAIL).
   * El canal de request se ignora: se usan los de "canalesSolicitados".
   *
   * - SISTEMA siempre se crea.
   * - Los demás canales (EMAIL, SMS) se crean SOLO si están disponibles
   * (habilitados y configurados); si no, se omiten sin error ni filas
   * FALLIDO. Así el llamador no necesita saber qué canales están activos.
   * - Si ya existe una notificación activa para ese canal, se omite (es
   * idempotente: volver a disparar el mismo evento no duplica).
   *
   * @return las notificaciones efectivamente creadas (puede ser una lista vacía)
   */
  List<NotificacionResponse> crearEnCanales(CrearNotificacionRequest request,
      Set<CanalNotificacion> canalesSolicitados);

  public NotificacionResponse crearYEnviarExterna(
      CrearNotificacionRequest request);

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
