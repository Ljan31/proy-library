package com.proyecto.fhce.library.services.library;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.ReglasPrestamoDTO;
import com.proyecto.fhce.library.dto.SancionDTO.EstadoSancionUsuarioDTO;
import com.proyecto.fhce.library.dto.request.library.CancelarReservaRequest;
import com.proyecto.fhce.library.dto.request.library.ConvertirReservaRequest;
import com.proyecto.fhce.library.dto.request.library.ReservaRequest;
import com.proyecto.fhce.library.dto.response.library.ReservaResponse;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionPrestamoResponseDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Reserva;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoReserva;
import com.proyecto.fhce.library.enums.TipoPrestamo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.mapper.ReservaMapper;
import com.proyecto.fhce.library.repositories.BibliotecaEncargadoRepository;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;
import com.proyecto.fhce.library.repositories.ReservaRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.loads.AsignacionReservaService;
import com.proyecto.fhce.library.services.loads.ConfiguracionPrestamoService;
import com.proyecto.fhce.library.services.loads.PrestamoService;
import com.proyecto.fhce.library.services.loads.SancionService;

@Service
@Transactional(readOnly = true)
public class ReservaServiceImpl implements ReservaService {

  private static final Logger log = LoggerFactory.getLogger(ReservaServiceImpl.class);

  @Autowired
  private ReservaRepository reservaRepository;
  @Autowired
  private UserRepository usuarioRepository;
  @Autowired
  private LibroRepository libroRepository;
  @Autowired
  private BibliotecaRepository bibliotecaRepository;
  @Autowired
  private EjemplarRepository ejemplarRepository;
  @Autowired
  private BibliotecaEncargadoRepository bibliotecaEncargadoRepository;
  @Autowired
  private ReservaMapper reservaMapper;

  @Autowired
  private ConfiguracionPrestamoService configuracionService;
  @Autowired
  private SancionService sancionService;
  @Autowired
  private PrestamoService prestamoService;

  @Autowired
  private AsignacionReservaService asignacionReservaService;
  // @Autowired private NotificacionService notificacionService;

  // ==================== QUERIES ====================

  public ReservaResponse findById(Long id) {
    Reserva reserva = reservaRepository.findByIdWithRelaciones(id)
        .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + id));
    return reservaMapper.toResponse(reserva);
  }

  public List<ReservaResponse> findByUsuario(Long usuarioId) {
    if (!usuarioRepository.existsById(usuarioId)) {
      throw new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId);
    }
    return reservaRepository.findByUsuarioWithRelaciones(usuarioId)
        .stream()
        .map(reservaMapper::toResponse)
        .collect(Collectors.toList());
  }

  public List<ReservaResponse> findByBiblioteca(Long bibliotecaId, EstadoReserva estado) {
    if (!bibliotecaRepository.existsById(bibliotecaId)) {
      throw new ResourceNotFoundException("Biblioteca no encontrada con id: " + bibliotecaId);
    }
    return reservaRepository.findByBibliotecaConFiltroEstado(bibliotecaId, estado)
        .stream()
        .map(reservaMapper::toResponse)
        .collect(Collectors.toList());
  }

  public List<ReservaResponse> findByEstado(EstadoReserva estado) {
    return reservaRepository.findByEstadoReserva(estado)
        .stream()
        .map(reservaMapper::toResponse)
        .collect(Collectors.toList());
  }

  public List<ReservaResponse> findColaEspera(Long libroId, Long bibliotecaId) {
    return reservaRepository.findColaEsperaByLibroAndBiblioteca(libroId, bibliotecaId)
        .stream()
        .map(reservaMapper::toResponse)
        .collect(Collectors.toList());
  }

  // ==================== OPERACIONES DE NEGOCIO ====================

  @Transactional
  public ReservaResponse crearReserva(ReservaRequest request, Long usuarioId) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    Libro libro = libroRepository.findById(request.getLibroId())
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + request.getLibroId()));

    Biblioteca biblioteca = bibliotecaRepository.findById(request.getBibliotecaId())
        .orElseThrow(
            () -> new ResourceNotFoundException("Biblioteca no encontrada con id: " + request.getBibliotecaId()));

    // ① Verificar suspensión vigente — INTEGRACIÓN CON SANCIONES
    EstadoSancionUsuarioDTO estadoSanciones = sancionService.obtenerEstadoSanciones(usuarioId);
    if (estadoSanciones.tieneSuspensionVigente()) {
      throw new BusinessException(
          "No puedes realizar reservas. Tienes una suspensión activa hasta: "
              + estadoSanciones.fechaFinSuspensionMasProxima());
    }

    // ② Verificar que no tenga ya una reserva activa del mismo libro en la misma
    // biblioteca
    if (reservaRepository.existsReservaActivaByUsuarioAndLibro(
        usuarioId, libro.getIdLibro(), biblioteca.getIdBiblioteca())) {
      throw new BusinessException(
          "Ya tienes una reserva activa para el libro '" + libro.getTitulo()
              + "' en esta biblioteca.");
    }

    // ③ Resolver configuración — INTEGRACIÓN CON CONFIGURACIÓN
    // Se usa TipoPrestamo.DOMICILIO para reservas (por defecto según la guía del
    // módulo)
    // ConfiguracionResueltaDTO config =
    // configuracionService.resolverConfiguracionAplicable(
    ReglasPrestamoDTO config = configuracionService.obtenerReglas(
        biblioteca.getIdBiblioteca(),
        TipoPrestamo.DOMICILIO);

    // ④ Validar límite de reservas simultáneas (si la configuración lo define)
    // El límite de ejemplares de la config se reutiliza como tope de reservas
    // simultáneas
    Long reservasActivas = reservaRepository.countReservasActivasByUsuarioAndBiblioteca(
        usuarioId, biblioteca.getIdBiblioteca());
    if (reservasActivas >= config.getEjemplaresPermitidos()) {
      throw new BusinessException(
          "Has alcanzado el límite de reservas simultáneas: "
              + config.getEjemplaresPermitidos());
    }

    // ⑤ Calcular prioridad FIFO automáticamente
    Integer maxPrioridad = reservaRepository.findMaxPrioridadByLibroAndBiblioteca(
        libro.getIdLibro(), biblioteca.getIdBiblioteca());
    int nuevaPrioridad = maxPrioridad + 1;

    // ⑥ Crear la reserva
    Reserva reserva = new Reserva();
    reserva.setUsuario(usuario);
    reserva.setLibro(libro);
    reserva.setBiblioteca(biblioteca);
    reserva.setFechaReserva(LocalDateTime.now());
    reserva.setEstadoReserva(EstadoReserva.ACTIVA);
    reserva.setPrioridad(nuevaPrioridad);
    reserva.setIdConfigUsado(config.getIdConfig()); // ← guardar para fechas futuras
    reserva.setObservaciones(request.getObservaciones());
    // fechaVencimientoReserva = null hasta que se asigne un ejemplar (estado
    // NOTIFICADA)
    // ejemplarId = null hasta asignación automática

    Reserva saved = reservaRepository.save(reserva);
    log.info("Reserva creada: id={} usuario={} libro='{}' prioridad={}",
        saved.getIdReserva(), usuarioId, libro.getTitulo(), nuevaPrioridad);

    // ⑦ Verificar si ya hay ejemplares disponibles para asignar inmediatamente
    // Se ejecuta fuera del flujo de guardado para que la reserva ya esté persistida
    try {
      asignacionReservaService.procesarDisponibilidad(libro.getIdLibro(), biblioteca.getIdBiblioteca());
    } catch (Exception e) {
      // No revierte la creación de la reserva si la asignación falla
      log.warn("No se pudo asignar ejemplar inmediatamente para reserva id={}: {}",
          saved.getIdReserva(), e.getMessage());
    }

    // Recargar para retornar con relaciones completas
    return reservaMapper.toResponse(
        reservaRepository.findByIdWithRelaciones(saved.getIdReserva()).orElse(saved));
  }

  @Transactional
  public ReservaResponse cancelarReserva(Long reservaId, Long usuarioId,
      boolean esBibliotecario,
      CancelarReservaRequest request) {
    Reserva reserva = reservaRepository.findByIdWithRelaciones(reservaId)
        .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + reservaId));

    // Validar autorización: solo el dueño o un bibliotecario de esa biblioteca
    boolean esPropietario = reserva.getUsuario().getId_usuario().equals(usuarioId);
    if (!esPropietario) {
      if (esBibliotecario) {
        validarEncargadoBiblioteca(usuarioId, reserva.getBiblioteca());
      } else {
        throw new BusinessException("No estás autorizado para cancelar esta reserva");
      }
    }

    // Solo se pueden cancelar reservas en estados activos
    if (reserva.getEstadoReserva() != EstadoReserva.ACTIVA
        && reserva.getEstadoReserva() != EstadoReserva.NOTIFICADA) {
      throw new BusinessException(
          "No se puede cancelar una reserva en estado: " + reserva.getEstadoReserva());
    }

    // Si tenía un ejemplar asignado, liberarlo y reasignar al siguiente en cola
    Ejemplar ejemplarAsignado = reserva.getEjemplar();
    if (ejemplarAsignado != null) {
      ejemplarAsignado.setEstadoEjemplar(EstadoEjemplar.DISPONIBLE);
      ejemplarRepository.save(ejemplarAsignado);
      log.info("Ejemplar id={} liberado al cancelar reserva id={}", ejemplarAsignado.getIdEjemplar(), reservaId);
    }

    if (request != null && request.getMotivo() != null) {
      reserva.setObservaciones(
          (reserva.getObservaciones() != null ? reserva.getObservaciones() + ". " : "")
              + "Cancelación: " + request.getMotivo());
    }

    reserva.setEstadoReserva(EstadoReserva.CANCELADA);
    Reserva updated = reservaRepository.save(reserva);
    log.info("Reserva cancelada: id={} usuario={}", reservaId, usuarioId);

    // Reasignar al siguiente en la cola si había ejemplar liberado
    if (ejemplarAsignado != null) {
      try {
        asignacionReservaService.procesarDisponibilidad(
            reserva.getLibro().getIdLibro(),
            reserva.getBiblioteca().getIdBiblioteca());
      } catch (Exception e) {
        log.warn("No se pudo reasignar ejemplar tras cancelación de reserva id={}: {}",
            reservaId, e.getMessage());
      }
    }

    return reservaMapper.toResponse(updated);
  }

  @Transactional
  public ReservaResponse convertirEnPrestamo(ConvertirReservaRequest request, Long bibliotecarioId) {
    Reserva reserva = reservaRepository.findByIdWithRelaciones(request.getReservaId())
        .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada con id: " + request.getReservaId()));

    // Solo se pueden convertir reservas NOTIFICADAS (tienen ejemplar asignado)
    if (reserva.getEstadoReserva() != EstadoReserva.NOTIFICADA) {
      throw new BusinessException(
          "Solo se pueden convertir reservas en estado NOTIFICADA. Estado actual: "
              + reserva.getEstadoReserva());
    }

    if (reserva.getEjemplar() == null) {
      throw new BusinessException("La reserva no tiene un ejemplar asignado. No puede convertirse en préstamo.");
    }

    validarEncargadoBiblioteca(bibliotecarioId, reserva.getBiblioteca());

    // ① Verificar que el usuario no tenga sanción nueva desde que se notificó
    EstadoSancionUsuarioDTO estadoSanciones = sancionService
        .obtenerEstadoSanciones(reserva.getUsuario().getId_usuario());
    if (estadoSanciones.tieneSuspensionVigente()) {
      throw new BusinessException(
          "El usuario tiene una suspensión activa. No se puede convertir la reserva en préstamo.");
    }

    // ② Delegar la creación del préstamo al módulo de Préstamos — INTEGRACIÓN
    // PrestamoService valida límites, condición del ejemplar y persiste el
    // préstamo.
    // No duplicamos esa lógica aquí.
    com.proyecto.fhce.library.dto.request.loads.PrestamoRequest prestamoRequest = new com.proyecto.fhce.library.dto.request.loads.PrestamoRequest();
    prestamoRequest.setUsuarioId(reserva.getUsuario().getId_usuario());
    prestamoRequest.setEjemplarId(reserva.getEjemplar().getIdEjemplar());
    prestamoRequest.setBibliotecaId(reserva.getBiblioteca().getIdBiblioteca());
    prestamoRequest.setTipoPrestamo(TipoPrestamo.DOMICILIO);
    prestamoRequest.setCondicionEntrega(request.getCondicionEntrega());
    prestamoRequest.setTipoDocumentoGarantia(request.getTipoDocumentoGarantia());
    prestamoRequest.setFechaDevolucionEstimada(request.getFechaDevolucionEstimada());
    prestamoRequest.setObservaciones(
        "Generado desde reserva #" + reserva.getIdReserva()
            + (request.getObservaciones() != null ? ". " + request.getObservaciones() : ""));

    var prestamoResponse = prestamoService.realizarPrestamo(prestamoRequest, bibliotecarioId);

    // ③ Actualizar la reserva con el préstamo generado
    // Necesitamos la entidad Prestamo para la relación — solo cargamos el proxy por
    // ID
    Prestamo prestamo = new Prestamo();
    prestamo.setIdPrestamo(prestamoResponse.getId_prestamo());
    reserva.setPrestamo(prestamo);
    reserva.setEstadoReserva(EstadoReserva.ATENDIDA);

    Reserva updated = reservaRepository.save(reserva);
    log.info("Reserva id={} convertida en préstamo id={}", reserva.getIdReserva(), prestamoResponse.getId_prestamo());

    return reservaMapper.toResponse(updated);
  }

  // ==================== PROCESO DE ASIGNACIÓN ====================

  // ==================== SCHEDULED JOBS ====================

  /**
   * Detecta reservas NOTIFICADAS cuya fecha de vencimiento ya pasó.
   * Las marca como VENCIDAS, libera el ejemplar y pasa al siguiente en la cola.
   *
   * Corre a las 00:30 AM (después del CRON de préstamos a las 01:00 AM es
   * redundante,
   * pero las reservas tienen su propio ciclo de vencimiento).
   */
  @Scheduled(cron = "0 05 11 * * *")
  @Transactional
  public void expirarReservasVencidas() {
    List<Reserva> vencidas = reservaRepository.findReservasVencidasParaExpirar(LocalDate.now());
    int procesadas = 0;

    for (Reserva reserva : vencidas) {
      try {
        Ejemplar ejemplar = reserva.getEjemplar();
        if (ejemplar != null) {
          ejemplar.setEstadoEjemplar(EstadoEjemplar.DISPONIBLE);
          ejemplarRepository.save(ejemplar);
        }

        reserva.setEstadoReserva(EstadoReserva.VENCIDA);
        reserva.setObservaciones(
            (reserva.getObservaciones() != null ? reserva.getObservaciones() + ". " : "")
                + "Reserva vencida automáticamente el " + LocalDate.now());
        reservaRepository.save(reserva);
        procesadas++;

        // notificacionService.enviarNotificacionReservaVencida(reserva);

        // Intentar asignar el ejemplar liberado al siguiente en la cola
        if (ejemplar != null) {
          asignacionReservaService.procesarDisponibilidad(
              reserva.getLibro().getIdLibro(),
              reserva.getBiblioteca().getIdBiblioteca());
        }

      } catch (Exception e) {
        // Un fallo en una reserva no detiene el procesamiento de las demás
        log.error("Error al expirar reserva id={}: {}", reserva.getIdReserva(), e.getMessage());
      }
    }

    log.info("CRON expirarReservasVencidas: {} reservas procesadas", procesadas);
  }

  /**
   * Detecta libros/bibliotecas con reservas ACTIVAS y ejemplares disponibles
   * que aún no fueron asignados (puede ocurrir si el CRON de préstamos y el
   * de reservas no se sincronizaron, o si un ejemplar fue devuelto fuera del
   * flujo de devolución normal).
   *
   * Corre a las 02:00 AM — posterior al CRON de expiración para que los
   * ejemplares liberados ya estén disponibles.
   */
  // @Scheduled(cron = "0 0 20 * * *")
  @Scheduled(cron = "0 59 10 * * *")
  @Transactional
  public void detectarYAsignarDisponiblesProgramado() {
    asignacionReservaService.detectarYAsignarDisponibles();
  }

  // ==================== PRIVATE HELPERS ====================

  private void validarEncargadoBiblioteca(Long bibliotecarioId, Biblioteca biblioteca) {
    boolean esEncargado = bibliotecaEncargadoRepository
        .existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(
            biblioteca.getIdBiblioteca(), bibliotecarioId);
    if (!esEncargado) {
      throw new BusinessException(
          "No estás autorizado para operar en la biblioteca: " + biblioteca.getNombre());
    }
  }
}