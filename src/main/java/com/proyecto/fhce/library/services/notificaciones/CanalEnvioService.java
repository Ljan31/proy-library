package com.proyecto.fhce.library.services.notificaciones;

import com.proyecto.fhce.library.entities.Notificacion;
import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;

/**
 * Contrato para los adaptadores de envío.
 *
 * Cada canal (EMAIL, SMS, SISTEMA) implementa esta interfaz.
 * El servicio principal no sabe nada de cómo se envía — solo
 * delega al canal correspondiente.
 *
 * Para añadir un nuevo canal (ej. PUSH), basta con crear una
 * nueva implementación. No se modifica nada más.
 */
public interface CanalEnvioService {

  /**
   * Canal que maneja esta implementación.
   */
  CanalNotificacion canal();

  /**
   * Envía la notificación por el canal correspondiente.
   * Lanza una excepción si el envío falla, para que el llamador
   * registre el estado FALLIDO con el mensaje de error.
   */
  void enviar(Notificacion notificacion);

  /**
   * Indica si el canal está disponible y configurado.
   * El servicio lo consulta antes de intentar enviar.
   */
  default boolean estaDisponible() {
    return true;
  }
}
