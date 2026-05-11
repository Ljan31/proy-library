package com.proyecto.fhce.library.services.loads;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.response.loads.ConfiguracionPrestamoResponseDTO;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Reserva;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoReserva;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.ReservaRepository;
import com.proyecto.fhce.library.services.library.ReservaServiceImpl;

@Service
public class AsignacionReservaService {
  private static final Logger log = LoggerFactory.getLogger(ReservaServiceImpl.class);
  @Autowired
  private ReservaRepository reservaRepository;
  @Autowired
  private EjemplarRepository ejemplarRepository;
  @Autowired
  private ConfiguracionPrestamoService configuracionService;

  @Transactional
  public void detectarYAsignarDisponibles() {
    List<Object[]> pendientes = reservaRepository
        .findLibrosBibliotecaConReservaActivaYEjemplarDisponible();

    log.info("CRON detectarYAsignarDisponibles: {} combinaciones libro/biblioteca a procesar",
        pendientes.size());
    int index = 0;
    for (Object[] fila : pendientes) {
      index++;
      Long libroId = (Long) fila[0];
      Long bibliotecaId = (Long) fila[1];
      log.debug("===============Procesando registro #{} -> libroId: {}, bibliotecaId: {}",
          index, libroId, bibliotecaId);
      try {
        long start = System.currentTimeMillis();

        procesarDisponibilidad(libroId, bibliotecaId);

        long end = System.currentTimeMillis();
        log.info("✔ Procesado OK -> libroId: {}, bibliotecaId: {} ({} ms)",
            libroId, bibliotecaId, (end - start));
      } catch (Exception e) {
        log.error("Error al procesar disponibilidad para libro={} biblioteca={}: {}",
            libroId, bibliotecaId, e.getMessage());
      }
    }
  }

  @Transactional
  public void procesarDisponibilidad(Long libroId, Long bibliotecaId) {
    log.info("INICIO procesarDisponibilidad libroId={}, bibliotecaId={}", libroId, bibliotecaId);
    int iteracion = 0;
    while (true) {
      iteracion++;
      log.info("---- Iteración {} ----", iteracion);

      // 1. Buscar ejemplar disponible con lock
      log.info("Buscando ejemplar DISPONIBLE con lock...");
      // Buscar primer ejemplar disponible del libro en la biblioteca
      List<Ejemplar> ejemplarDisponible = ejemplarRepository
          .findDisponibleConLock(
              libroId, bibliotecaId, EstadoEjemplar.DISPONIBLE, PageRequest.of(0, 1));

      if (ejemplarDisponible.isEmpty()) {
        log.info("No hay ejemplares disponibles para libroId={}, bibliotecaId={}", libroId, bibliotecaId);
        return; // No hay ejemplares disponibles, nada que hacer
      }

      // Tomar el primero en la cola CON LOCKING PESIMISTA
      // Evita que dos hilos asignen el mismo ejemplar a reservas distintas
      List<Reserva> primeraEnCola = reservaRepository.findPrimeraEnColaConLock(libroId, bibliotecaId,
          PageRequest.of(0, 1));
      log.info("DEBUG -> reservas en cola encontradas: {}", primeraEnCola.size());
      for (Reserva r : primeraEnCola) {
        log.info("DEBUG -> Reserva id={}, usuarioId={}, estado={}, fecha={}",
            r.getIdReserva(),
            r.getUsuario().getId_usuario(),
            r.getEstadoReserva(),
            r.getFechaReserva());
      }
      if (primeraEnCola.isEmpty()) {
        log.info("No hay reservas en cola para libroId={}, bibliotecaId={}", libroId, bibliotecaId);

        return; // No hay nadie esperando este libro
      }

      Reserva reserva = primeraEnCola.get(0);
      Ejemplar ejemplar = ejemplarDisponible.get(0);

      log.info("Reserva encontrada: idReserva={}, usuarioId={}, estado={}",
          reserva.getIdReserva(),
          reserva.getUsuario().getId_usuario(),
          reserva.getEstadoReserva());

      log.info("Ejemplar encontrado y bloqueado: idEjemplar={}, estado={}",
          ejemplar.getIdEjemplar(), ejemplar.getEstadoEjemplar());

      // ConfiguracionResueltaDTO config =
      // configuracionService.resolverConfiguracionPorId(
      ConfiguracionPrestamoResponseDTO config = configuracionService.buscarPorId(
          reserva.getIdConfigUsado());
      log.info("Configuración obtenida: diasReserva={}", config.getDiasReserva());
      // Asignar ejemplar y cambiar estado
      ejemplar.setEstadoEjemplar(EstadoEjemplar.RESERVADO);
      // ejemplarRepository.save(ejemplar);
      ejemplarRepository.saveAndFlush(ejemplar);

      reserva.setEjemplar(ejemplar);
      reserva.setEstadoReserva(EstadoReserva.NOTIFICADA);
      // fecha_vencimiento_reserva = hoy + diasReserva de la config guardada
      reserva.setFechaVencimientoReserva(LocalDate.now().plusDays(config.getDiasReserva()));
      // reservaRepository.save(reserva);
      reservaRepository.saveAndFlush(reserva);

      log.info("Ejemplar id={} asignado a reserva id={} (usuario={}). Vence: {}",
          ejemplar.getIdEjemplar(), reserva.getIdReserva(),
          reserva.getUsuario().getId_usuario(),
          reserva.getFechaVencimientoReserva());
      log.debug("Post-save -> ejemplar estado={}, reserva estado={}",
          ejemplar.getEstadoEjemplar(),
          reserva.getEstadoReserva());
      // Notificar al usuario — INTEGRACIÓN CON NOTIFICACIONES
      // notificacionService.enviarNotificacionReservaDisponible(reserva);
    }

  }

}
