package com.proyecto.fhce.library.services.library;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.library.EjemplarRequest;
import com.proyecto.fhce.library.dto.request.library.EjemplarUpdateEstadoRequest;
import com.proyecto.fhce.library.dto.response.library.BibliotecaSimpleResponse;
import com.proyecto.fhce.library.dto.response.library.DisponibilidadLibroResponse;
import com.proyecto.fhce.library.dto.response.library.DisponibilidadPorBibliotecaResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.dto.response.library.LibroSimpleResponse;
import com.proyecto.fhce.library.dto.response.loads.HistorialEstadoResponse;
import com.proyecto.fhce.library.dto.response.loads.PrestamoActivoResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.HistorialEstadoEjemplar;
import com.proyecto.fhce.library.entities.Libro;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.HistorialEstadoEjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;

@Service
@Transactional
public class EjemplarService {

  @Autowired
  private EjemplarRepository ejemplarRepository;

  @Autowired
  private LibroRepository libroRepository;

  @Autowired
  private BibliotecaRepository bibliotecaRepository;

  @Autowired
  private HistorialEstadoEjemplarRepository historialRepository;

  @Autowired
  private PrestamoRepository prestamoRepository;

  // @Autowired
  // private AuditoriaService auditoriaService;

  public EjemplarResponse create(EjemplarRequest request) {
    // Validar código único
    if (ejemplarRepository.existsByCodigoEjemplar(request.getCodigo_ejemplar())) {
      throw new DuplicateResourceException("Ya existe un ejemplar con código: " +
          request.getCodigo_ejemplar());
    }

    Libro libro = libroRepository.findById(request.getLibroId())
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con id: " + request.getLibroId()));

    Biblioteca biblioteca = bibliotecaRepository.findById(request.getBibliotecaId())
        .orElseThrow(
            () -> new ResourceNotFoundException("Biblioteca no encontrada con id: " + request.getBibliotecaId()));

    Ejemplar ejemplar = new Ejemplar();
    ejemplar.setLibro(libro);
    ejemplar.setBiblioteca(biblioteca);
    ejemplar.setCodigoEjemplar(request.getCodigo_ejemplar());
    ejemplar.setCodigoTopografico(request.getCodigo_topografico());
    ejemplar.setUbicacionFisica(request.getUbicacion_fisica());
    ejemplar.setEstadoEjemplar(
        request.getEstadoEjemplar() != null ? request.getEstadoEjemplar() : EstadoEjemplar.DISPONIBLE);
    ejemplar
        .setFechaAdquisicion(request.getFechaAdquisicion() != null ? request.getFechaAdquisicion() : LocalDate.now());
    ejemplar.setPrecioCompra(request.getPrecio_compra());
    ejemplar.setObservaciones(request.getObservaciones());

    Ejemplar saved = ejemplarRepository.save(ejemplar);

    // Registrar en historial
    registrarCambioEstado(saved, null, saved.getEstadoEjemplar(), "Registro inicial");

    // auditoriaService.registrar("CREATE_COPY", "copies",
    // saved.getId_ejemplar(), null, saved.getCodigo_ejemplar());

    return mapToResponse(saved);
  }

  public EjemplarResponse update(Long id, EjemplarRequest request) {
    Ejemplar ejemplar = ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado con id: " + id));

    // Validar código único si cambió
    if (!ejemplar.getCodigoEjemplar().equals(request.getCodigo_ejemplar()) &&
        ejemplarRepository.existsByCodigoEjemplar(request.getCodigo_ejemplar())) {
      throw new DuplicateResourceException("Ya existe un ejemplar con código: " +
          request.getCodigo_ejemplar());
    }

    ejemplar.setCodigoEjemplar(request.getCodigo_ejemplar());
    ejemplar.setCodigoTopografico(request.getCodigo_topografico());
    ejemplar.setUbicacionFisica(request.getUbicacion_fisica());
    ejemplar.setObservaciones(request.getObservaciones());

    if (request.getPrecio_compra() != null) {
      ejemplar.setPrecioCompra(request.getPrecio_compra());
    }

    // Actualizar libro y biblioteca si cambiaron
    if (request.getLibroId() != null && !ejemplar.getLibro().getId_libro().equals(request.getLibroId())) {
      Libro libro = libroRepository.findById(request.getLibroId())
          .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));
      ejemplar.setLibro(libro);
    }

    if (request.getBibliotecaId() != null
        && !ejemplar.getBiblioteca().getId_biblioteca().equals(request.getBibliotecaId())) {
      Biblioteca biblioteca = bibliotecaRepository.findById(request.getBibliotecaId())
          .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));
      ejemplar.setBiblioteca(biblioteca);
    }

    Ejemplar updated = ejemplarRepository.save(ejemplar);

    // auditoriaService.registrar("UPDATE_COPY", "copies",
    // updated.getId_ejemplar(), null, updated.getCodigo_ejemplar());

    return mapToResponse(updated);
  }

  public EjemplarResponse actualizarEstado(Long id, EjemplarUpdateEstadoRequest request) {
    Ejemplar ejemplar = ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado con id: " + id));

    // Validar cambio de estado
    validarCambioEstado(ejemplar, request.getNuevoEstado());

    EstadoEjemplar estadoAnterior = ejemplar.getEstadoEjemplar();
    ejemplar.setEstadoEjemplar(request.getNuevoEstado());

    Ejemplar updated = ejemplarRepository.save(ejemplar);

    // Registrar en historial
    registrarCambioEstado(updated, estadoAnterior, request.getNuevoEstado(), request.getMotivo());

    // auditoriaService.registrar("UPDATE_COPY_STATUS", "copies",
    // updated.getId_ejemplar(), estadoAnterior, request.getNuevoEstado());

    return mapToResponse(updated);
  }

  public void darDeBaja(Long id, String motivo) {
    Ejemplar ejemplar = ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado con id: " + id));

    // Validar que no esté prestado
    if (ejemplar.getEstadoEjemplar() == EstadoEjemplar.PRESTADO) {
      throw new BusinessException("No se puede dar de baja un ejemplar prestado");
    }

    EstadoEjemplar estadoAnterior = ejemplar.getEstadoEjemplar();
    ejemplar.setEstadoEjemplar(EstadoEjemplar.BAJA);

    ejemplarRepository.save(ejemplar);

    registrarCambioEstado(ejemplar, estadoAnterior, EstadoEjemplar.BAJA, motivo);

    // auditoriaService.registrar("DEACTIVATE_COPY", "copies",
    // ejemplar.getId_ejemplar(), null, "Motivo: " + motivo);
  }

  public void reportarPerdido(Long id, String motivo) {
    Ejemplar ejemplar = ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado"));

    EstadoEjemplar estadoAnterior = ejemplar.getEstadoEjemplar();
    ejemplar.setEstadoEjemplar(EstadoEjemplar.PERDIDO);

    ejemplarRepository.save(ejemplar);

    registrarCambioEstado(ejemplar, estadoAnterior, EstadoEjemplar.PERDIDO, motivo);

    // auditoriaService.registrar("REPORT_COPY_LOST", "copies",
    // ejemplar.getId_ejemplar(), null, "Motivo: " + motivo);
  }

  public void transferirBiblioteca(Long ejemplarId, Long nuevaBibliotecaId, String motivo) {
    Ejemplar ejemplar = ejemplarRepository.findById(ejemplarId)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado"));

    // Validar que no esté prestado
    if (ejemplar.getEstadoEjemplar() == EstadoEjemplar.PRESTADO) {
      throw new BusinessException("No se puede transferir un ejemplar prestado");
    }

    Biblioteca nuevaBiblioteca = bibliotecaRepository.findById(nuevaBibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

    Biblioteca bibliotecaAnterior = ejemplar.getBiblioteca();
    ejemplar.setBiblioteca(nuevaBiblioteca);

    ejemplarRepository.save(ejemplar);

    // auditoriaService.registrar("TRANSFER_COPY", "copies",
    // ejemplar.getId_ejemplar(),
    // bibliotecaAnterior.getNombre(),
    // nuevaBiblioteca.getNombre() + ". Motivo: " + motivo);
  }

  @Transactional(readOnly = true)
  public List<EjemplarResponse> findAll() {
    return ejemplarRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public EjemplarResponse findById(Long id) {
    Ejemplar ejemplar = ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado con id: " + id));
    return mapToResponse(ejemplar);
  }

  @Transactional(readOnly = true)
  public EjemplarResponse findByCodigo(String codigo) {
    Ejemplar ejemplar = ejemplarRepository.findByCodigoEjemplar(codigo)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado con código: " + codigo));
    return mapToResponse(ejemplar);
  }

  @Transactional(readOnly = true)
  public List<EjemplarResponse> findByLibro(Long libroId) {
    return ejemplarRepository.findByLibro_IdLibro(libroId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<EjemplarResponse> findByBiblioteca(Long bibliotecaId) {
    return ejemplarRepository.findByBiblioteca_IdBiblioteca(bibliotecaId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<EjemplarResponse> findByEstado(EstadoEjemplar estado) {
    return ejemplarRepository.findByEstadoEjemplar(estado).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<EjemplarResponse> findDisponiblesByLibro(Long libroId) {
    return ejemplarRepository.findEjemplaresDisponiblesByLibro(libroId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<EjemplarResponse> findByBibliotecaAndEstado(Long bibliotecaId, EstadoEjemplar estado) {
    return ejemplarRepository.findByBibliotecaAndEstadoIn(
        bibliotecaId,
        Collections.singletonList(estado)).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<HistorialEstadoResponse> obtenerHistorial(Long ejemplarId) {
    List<HistorialEstadoEjemplar> historial = historialRepository
        .findByEjemplarOrderByFechaDesc(ejemplarId);

    return historial.stream()
        .map(this::mapHistorialToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public DisponibilidadLibroResponse verificarDisponibilidad(Long libroId) {
    Libro libro = libroRepository.findById(libroId)
        .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado"));

    List<Ejemplar> ejemplares = ejemplarRepository.findByLibro_IdLibro(libroId);

    DisponibilidadLibroResponse disponibilidad = new DisponibilidadLibroResponse();
    disponibilidad.setLibroId(libroId);
    disponibilidad.setTituloLibro(libro.getTitulo());
    disponibilidad.setTotalEjemplares(ejemplares.size());

    long disponibles = ejemplares.stream()
        .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.DISPONIBLE)
        .count();
    disponibilidad.setEjemplaresDisponibles((int) disponibles);

    long prestados = ejemplares.stream()
        .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.PRESTADO)
        .count();
    disponibilidad.setEjemplaresPrestados((int) prestados);

    long reservados = ejemplares.stream()
        .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.RESERVADO)
        .count();
    disponibilidad.setEjemplaresReservados((int) reservados);

    disponibilidad.setHayDisponibles(disponibles > 0);

    // Agrupar por biblioteca
    Map<Biblioteca, List<Ejemplar>> porBiblioteca = ejemplares.stream()
        .collect(Collectors.groupingBy(Ejemplar::getBiblioteca));

    List<DisponibilidadPorBibliotecaResponse> porBiblio = porBiblioteca.entrySet().stream()
        .map(entry -> {
          DisponibilidadPorBibliotecaResponse db = new DisponibilidadPorBibliotecaResponse();
          BibliotecaSimpleResponse biblio = new BibliotecaSimpleResponse();
          biblio.setId_biblioteca(entry.getKey().getId_biblioteca());
          biblio.setNombre(entry.getKey().getNombre());
          biblio.setTipoBiblioteca(entry.getKey().getTipoBiblioteca());
          db.setBiblioteca(biblio);

          db.setTotal(entry.getValue().size());
          long disp = entry.getValue().stream()
              .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.DISPONIBLE)
              .count();
          db.setDisponibles((int) disp);

          return db;
        })
        .collect(Collectors.toList());

    disponibilidad.setPorBiblioteca(porBiblio);

    return disponibilidad;
  }

  private void validarCambioEstado(Ejemplar ejemplar, EstadoEjemplar nuevoEstado) {
    EstadoEjemplar estadoActual = ejemplar.getEstadoEjemplar();

    // Si está prestado, solo puede cambiar a: PRESTADO (mismo), PERDIDO, o ser
    // devuelto (DISPONIBLE/EN_REPARACION)
    if (estadoActual == EstadoEjemplar.PRESTADO) {
      if (nuevoEstado != EstadoEjemplar.DISPONIBLE &&
          nuevoEstado != EstadoEjemplar.EN_REPARACION &&
          nuevoEstado != EstadoEjemplar.PERDIDO &&
          nuevoEstado != EstadoEjemplar.PRESTADO) {
        throw new BusinessException("Un ejemplar prestado solo puede cambiar a DISPONIBLE, EN_REPARACION o PERDIDO");
      }
    }

    // Si está dado de baja, no puede cambiar de estado
    if (estadoActual == EstadoEjemplar.BAJA) {
      throw new BusinessException("Un ejemplar dado de baja no puede cambiar de estado");
    }
  }

  private void registrarCambioEstado(Ejemplar ejemplar, EstadoEjemplar estadoAnterior,
      EstadoEjemplar estadoNuevo, String motivo) {
    HistorialEstadoEjemplar historial = new HistorialEstadoEjemplar();
    historial.setEjemplar(ejemplar);
    historial.setEstadoAnterior(estadoAnterior);
    historial.setEstadoNuevo(estadoNuevo);
    historial.setMotivo(motivo);
    // historial.setUsuarioCambio(SecurityUtils.getCurrentUser()); // Usuario actual

    historialRepository.save(historial);
  }

  private EjemplarResponse mapToResponse(Ejemplar ejemplar) {
    EjemplarResponse response = new EjemplarResponse();
    response.setId_ejemplar(ejemplar.getIdEjemplar());
    response.setCodigo_ejemplar(ejemplar.getCodigoEjemplar());
    response.setCodigo_topografico(ejemplar.getCodigoTopografico());
    response.setUbicacion_fisica(ejemplar.getUbicacionFisica());
    response.setEstadoEjemplar(ejemplar.getEstadoEjemplar());
    response.setFechaAdquisicion(ejemplar.getFechaAdquisicion());
    response.setPrecio_compra(ejemplar.getPrecioCompra());
    response.setObservaciones(ejemplar.getObservaciones());

    // Libro
    if (ejemplar.getLibro() != null) {
      LibroSimpleResponse libro = new LibroSimpleResponse();
      libro.setId_libro(ejemplar.getLibro().getId_libro());
      libro.setIsbn(ejemplar.getLibro().getIsbn());
      libro.setTitulo(ejemplar.getLibro().getTitulo());
      libro.setEditorial(ejemplar.getLibro().getEditorial());
      libro.setAnoPublicacion(ejemplar.getLibro().getAnoPublicacion());
      libro.setImagen_portada(ejemplar.getLibro().getImagen_portada());
      // autores

      response.setLibro(libro);
    }

    // Biblioteca
    if (ejemplar.getBiblioteca() != null) {
      BibliotecaSimpleResponse biblioteca = new BibliotecaSimpleResponse();
      biblioteca.setId_biblioteca(ejemplar.getBiblioteca().getId_biblioteca());
      biblioteca.setNombre(ejemplar.getBiblioteca().getNombre());
      biblioteca.setTipoBiblioteca(ejemplar.getBiblioteca().getTipoBiblioteca());
      response.setBiblioteca(biblioteca);
    }

    // Préstamo activo si está prestado
    if (ejemplar.getEstadoEjemplar() == EstadoEjemplar.PRESTADO) {
      Optional<Prestamo> prestamoActivo = prestamoRepository
          .findPrestamoActivoByEjemplar(ejemplar.getIdEjemplar());

      if (prestamoActivo.isPresent()) {
        Prestamo p = prestamoActivo.get();
        PrestamoActivoResponse prestamoResp = new PrestamoActivoResponse();
        prestamoResp.setId_prestamo(p.getId_prestamo());
        prestamoResp.setFechaPrestamo(p.getFechaPrestamo());
        prestamoResp.setFechaDevolucionEstimada(p.getFechaDevolucionEstimada());

        long diasRestantes = ChronoUnit.DAYS.between(LocalDate.now(), p.getFechaDevolucionEstimada());
        prestamoResp.setDiasRestantes((int) diasRestantes);
        prestamoResp.setVencido(diasRestantes < 0);

        response.setPrestamoActivo(prestamoResp);
      }
    }

    return response;
  }

  private HistorialEstadoResponse mapHistorialToResponse(HistorialEstadoEjemplar historial) {
    HistorialEstadoResponse response = new HistorialEstadoResponse();
    response.setId_historial(historial.getIdHistorial());
    response.setEstadoAnterior(historial.getEstadoAnterior());
    response.setEstadoNuevo(historial.getEstadoNuevo());
    response.setFechaCambio(historial.getFechaCambio());
    response.setMotivo(historial.getMotivo());

    if (historial.getUsuarioCambio() != null) {
      UsuarioSimpleResponse usuario = new UsuarioSimpleResponse();
      usuario.setId_usuario(historial.getUsuarioCambio().getId_usuario());
      usuario.setUsername(historial.getUsuarioCambio().getUsername());
      usuario.setNombreCompleto(
          historial.getUsuarioCambio().getPersona().getNombre() + " " +
              historial.getUsuarioCambio().getPersona().getApellido_pat());
      response.setUsuarioCambio(usuario);
    }

    return response;
  }
}