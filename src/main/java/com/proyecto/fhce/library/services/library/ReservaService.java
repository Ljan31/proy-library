package com.proyecto.fhce.library.services.library;

import java.util.List;

import com.proyecto.fhce.library.dto.request.library.CancelarReservaRequest;
import com.proyecto.fhce.library.dto.request.library.ConvertirReservaRequest;
import com.proyecto.fhce.library.dto.request.library.ReservaRequest;
import com.proyecto.fhce.library.dto.response.library.ReservaResponse;
import com.proyecto.fhce.library.enums.EstadoReserva;

public interface ReservaService {

  // ─── Queries ────────────────────────────────────────────────────────────

  ReservaResponse findById(Long id);

  List<ReservaResponse> findByUsuario(Long usuarioId);

  List<ReservaResponse> findByBiblioteca(Long bibliotecaId, EstadoReserva estado);

  List<ReservaResponse> findByEstado(EstadoReserva estado);

  /**
   * Cola de espera para un libro en una biblioteca, ordenada por prioridad ASC.
   * Útil para que el bibliotecario vea quién está esperando.
   */
  List<ReservaResponse> findColaEspera(Long libroId, Long bibliotecaId);

  // ─── Operaciones de negocio ──────────────────────────────────────────────

  /**
   * Crea una nueva reserva para el usuario indicado.
   *
   * Valida:
   * ① Que el usuario no tenga sanciones activas (vía SancionService)
   * ② Que no tenga ya una reserva activa del mismo libro en la misma biblioteca
   * ③ El límite de reservas simultáneas (vía ConfiguracionPrestamoService)
   * ④ Asigna prioridad FIFO automáticamente
   * ⑤ Guarda idConfigUsado para calcular fechas con la config vigente
   * ⑥ Si hay ejemplares disponibles, asigna inmediatamente y notifica
   */
  ReservaResponse crearReserva(ReservaRequest request, Long usuarioId);

  /**
   * Cancela una reserva ACTIVA o NOTIFICADA.
   *
   * Si la reserva tiene un ejemplar asignado (estado NOTIFICADA),
   * lo libera (EstadoEjemplar → DISPONIBLE) y reasigna al siguiente en la cola.
   *
   * Solo el dueño de la reserva o un bibliotecario de esa biblioteca puede
   * cancelar.
   */
  ReservaResponse cancelarReserva(Long reservaId, Long usuarioId, boolean esBibliotecario,
      CancelarReservaRequest request);

  /**
   * Convierte una reserva NOTIFICADA en préstamo.
   * El usuario se presenta a recoger el libro.
   *
   * Delega la creación del préstamo a PrestamoService.realizarPrestamo().
   * Actualiza el estado de la reserva a ATENDIDA y guarda el préstamo generado.
   *
   * Solo puede ejecutarlo un bibliotecario de la biblioteca correspondiente.
   */
  ReservaResponse convertirEnPrestamo(ConvertirReservaRequest request, Long bibliotecarioId);

  // ─── Procesos automáticos (llamados desde CRON) ──────────────────────────

  /**
   * Intenta asignar un ejemplar disponible a la primera reserva en cola
   * para el libro y biblioteca indicados.
   *
   * Usa locking pesimista para garantizar que dos hilos no asignen
   * el mismo ejemplar a dos reservas distintas.
   *
   * Llamado por el CRON de asignación y también desde PrestamoService
   * al registrar una devolución (el ejemplar queda disponible).
   */
  // void procesarDisponibilidad(Long libroId, Long bibliotecaId);
}