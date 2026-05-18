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
import com.proyecto.fhce.library.dto.response.library.EdicionSimpleResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.dto.response.loads.HistorialEstadoResponse;
import com.proyecto.fhce.library.dto.response.loads.PrestamoActivoResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Edicion;
import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.HistorialEstadoEjemplar;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.EdicionRepository;
import com.proyecto.fhce.library.repositories.EjemplarRepository;
import com.proyecto.fhce.library.repositories.HistorialEstadoEjemplarRepository;
import com.proyecto.fhce.library.repositories.LibroRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.ReservaRepository;
import com.proyecto.fhce.library.security.SecurityService;

@Service
@Transactional
public class EjemplarServiceImpl implements EjemplarService {
  @Autowired
  private EjemplarRepository ejemplarRepository;

  @Autowired
  private EdicionRepository edicionRepository;
  @Autowired
  private LibroRepository libroRepository;

  @Autowired
  private BibliotecaRepository bibliotecaRepository;

  @Autowired
  private HistorialEstadoEjemplarRepository historialRepository;

  @Autowired
  private PrestamoRepository prestamoRepository;

  @Autowired
  private ReservaRepository reservaRepository;

  private final SecurityService securityService;

  // @Autowired
  // private AuditoriaService auditoriaService;
  public EjemplarServiceImpl(SecurityService securityService) {
    this.securityService = securityService;
  }

  public EjemplarResponse create(EjemplarRequest request) {
    // Validar código único
    if (ejemplarRepository.existsByCodigoEjemplar(request.getCodigoEjemplar())) {
      throw new DuplicateResourceException("Ya existe un ejemplar con código: " + request.getCodigoEjemplar());
    }
    Edicion edicion = edicionRepository.findById(request.getEdicionId())
        .orElseThrow(() -> new ResourceNotFoundException("Edición no encontrada con id: " + request.getEdicionId()));

    Biblioteca biblioteca = bibliotecaRepository.findById(request.getBibliotecaId())
        .orElseThrow(
            () -> new ResourceNotFoundException("Biblioteca no encontrada con id: " + request.getBibliotecaId()));

    Ejemplar ejemplar = new Ejemplar();
    ejemplar.setEdicion(edicion); // ✅
    ejemplar.setBiblioteca(biblioteca);
    ejemplar.setCodigoEjemplar(request.getCodigoEjemplar());
    ejemplar.setCodigoTopografico(request.getCodigoTopografico());
    ejemplar.setClasificacionDecimal(request.getClasificacionDecimal());
    ejemplar.setCutterAutor(request.getCutterAutor());
    ejemplar.setCutterTitulo(request.getCutterTitulo());
    ejemplar.setUbicacionFisica(request.getUbicacionFisica());
    ejemplar.setEstadoEjemplar(
        request.getEstadoEjemplar() != null ? request.getEstadoEjemplar() : EstadoEjemplar.DISPONIBLE);
    ejemplar.setFechaAdquisicion(
        request.getFechaAdquisicion() != null ? request.getFechaAdquisicion() : LocalDate.now());
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

    if (!ejemplar.getCodigoEjemplar().equals(request.getCodigoEjemplar()) &&
        ejemplarRepository.existsByCodigoEjemplar(request.getCodigoEjemplar())) {
      throw new DuplicateResourceException("Ya existe un ejemplar con código: " + request.getCodigoEjemplar());
    }

    ejemplar.setCodigoEjemplar(request.getCodigoEjemplar());
    ejemplar.setCodigoTopografico(request.getCodigoTopografico());
    ejemplar.setClasificacionDecimal(request.getClasificacionDecimal());
    ejemplar.setCutterAutor(request.getCutterAutor());
    ejemplar.setCutterTitulo(request.getCutterTitulo());
    ejemplar.setUbicacionFisica(request.getUbicacionFisica());
    ejemplar.setObservaciones(request.getObservaciones());

    // Actualizar libro y biblioteca si cambiaron
    if (request.getEdicionId() != null &&
        !ejemplar.getEdicion().getIdEdicion().equals(request.getEdicionId())) {
      Edicion edicion = edicionRepository.findById(request.getEdicionId())
          .orElseThrow(() -> new ResourceNotFoundException("Edición no encontrada"));
      ejemplar.setEdicion(edicion);
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

  public void reportarDañado(Long id, String motivo) {
    Ejemplar ejemplar = getOrThrow(id);
    EstadoEjemplar anterior = ejemplar.getEstadoEjemplar();
    validarCambioEstado(ejemplar, EstadoEjemplar.DAÑADO);
    ejemplar.setEstadoEjemplar(EstadoEjemplar.DAÑADO);
    ejemplarRepository.save(ejemplar);
    registrarCambioEstado(ejemplar, anterior, EstadoEjemplar.DAÑADO, motivo);
  }

  public void darDeBaja(Long id, String motivo) {
    Ejemplar ejemplar = ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado con id: " + id));

    // Validar que no esté prestado
    if (ejemplar.getEstadoEjemplar() == EstadoEjemplar.PRESTADO) {
      throw new BusinessException("No se puede dar de baja un ejemplar prestado");
    }

    // Validar que el estado no sea igual al nuevo
    EstadoEjemplar estadoAnterior = ejemplar.getEstadoEjemplar();
    validarCambioEstado(ejemplar, EstadoEjemplar.BAJA);

    ejemplar.setEstadoEjemplar(EstadoEjemplar.BAJA);

    ejemplarRepository.save(ejemplar);

    registrarCambioEstado(ejemplar, estadoAnterior, EstadoEjemplar.BAJA, motivo);

    // auditoriaService.registrar("DEACTIVATE_COPY", "copies",
    // ejemplar.getId_ejemplar(), null, "Motivo: " + motivo);
  }

  public void reportarPerdido(Long id, String motivo) {
    Ejemplar ejemplar = ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado"));

    // Validar que el estado no sea igual al nuevo
    EstadoEjemplar estadoAnterior = ejemplar.getEstadoEjemplar();
    validarCambioEstado(ejemplar, EstadoEjemplar.PERDIDO);

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
  public List<EjemplarResponse> findByEdicion(Long edicionId) {
    return ejemplarRepository.findByEdicion_IdEdicion(edicionId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<EjemplarResponse> findByLibro(Long libroId) {
    return ejemplarRepository.findByLibroId(libroId).stream()
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
  public List<EjemplarResponse> findByLibroAndBibliotecaAndEstado(Long libroId, Long bibliotecaId,
      EstadoEjemplar estado) {
    return ejemplarRepository.findByLibroAndBibliotecaAndEstado(libroId, bibliotecaId, estado).stream()
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
  public DisponibilidadLibroResponse verificarDisponibilidad(Long libroId) {
    // ✅ Recupera todos los ejemplares del libro via sus ediciones
    List<Ejemplar> ejemplares = ejemplarRepository.findByLibroId(libroId);

    DisponibilidadLibroResponse disponibilidad = new DisponibilidadLibroResponse();
    disponibilidad.setLibroId(libroId);
    disponibilidad.setTotalEjemplares(ejemplares.size());

    disponibilidad.setEjemplaresDisponibles(contarPorEstado(ejemplares, EstadoEjemplar.DISPONIBLE));
    disponibilidad.setEjemplaresPrestados(contarPorEstado(ejemplares, EstadoEjemplar.PRESTADO));
    disponibilidad.setEjemplaresReservados(contarPorEstado(ejemplares, EstadoEjemplar.RESERVADO));
    disponibilidad.setEjemplaresPerdidos(contarPorEstado(ejemplares, EstadoEjemplar.PERDIDO));
    disponibilidad.setEjemplaresDañados(contarPorEstado(ejemplares, EstadoEjemplar.DAÑADO));
    disponibilidad.setEjemplaresEnReparacion(contarPorEstado(ejemplares, EstadoEjemplar.EN_REPARACION));
    disponibilidad.setHayDisponibles(disponibilidad.getEjemplaresDisponibles() > 0);

    Map<Biblioteca, List<Ejemplar>> porBiblioteca = ejemplares.stream()
        .collect(Collectors.groupingBy(Ejemplar::getBiblioteca));

    List<DisponibilidadPorBibliotecaResponse> porBiblio = porBiblioteca.entrySet().stream()
        .map(entry -> {
          DisponibilidadPorBibliotecaResponse db = new DisponibilidadPorBibliotecaResponse();
          BibliotecaSimpleResponse biblio = new BibliotecaSimpleResponse();
          biblio.setId_biblioteca(entry.getKey().getIdBiblioteca());
          biblio.setNombre(entry.getKey().getNombre());
          biblio.setTipoBiblioteca(entry.getKey().getTipoBiblioteca());
          db.setBiblioteca(biblio);
          db.setTotal(entry.getValue().size());
          long disp = entry.getValue().stream()
              .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.DISPONIBLE).count();
          db.setDisponibles((int) disp);
          return db;
        }).collect(Collectors.toList());

    disponibilidad.setPorBiblioteca(porBiblio);
    return disponibilidad;
  }

  @Transactional(readOnly = true)
  public List<HistorialEstadoResponse> obtenerHistorial(Long ejemplarId) {
    List<HistorialEstadoEjemplar> historial = historialRepository
        .findByEjemplarOrderByFechaDesc(ejemplarId);

    return historial.stream()
        .map(this::mapHistorialToResponse)
        .collect(Collectors.toList());
  }

  @Transactional
  public void delete(Long id) {

    Ejemplar ejemplar = ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Ejemplar no encontrado con id: " + id));

    // Validar que no esté prestado actualmente
    if (ejemplar.getEstadoEjemplar() == EstadoEjemplar.PRESTADO) {
      throw new BusinessException(
          "No se puede eliminar un ejemplar prestado");
    }

    // Validar préstamos asociados
    boolean tienePrestamos = prestamoRepository
        .existsByEjemplar_IdEjemplar(id);

    if (tienePrestamos) {
      throw new BusinessException(
          "No se puede eliminar un ejemplar con préstamos registrados");
    }

    // Validar reservas asociadas
    boolean tieneReservas = reservaRepository
        .existsByEjemplar_IdEjemplar(id);

    if (tieneReservas) {
      throw new BusinessException(
          "No se puede eliminar un ejemplar con reservas registradas");
    }

    ejemplarRepository.delete(ejemplar);
  }

  private void validarCambioEstado(Ejemplar ejemplar, EstadoEjemplar nuevoEstado) {
    EstadoEjemplar estadoActual = ejemplar.getEstadoEjemplar();
    // 🔹 No permitir mismo estado
    if (estadoActual == nuevoEstado) {
      throw new BusinessException(
          "El ejemplar ya se encuentra en estado " + estadoActual);
    }

    // 🔹 Si está dado de baja, no puede cambiar nunca
    if (estadoActual == EstadoEjemplar.BAJA) {
      throw new BusinessException(
          "Un ejemplar dado de baja no puede cambiar de estado");
    }
    // Si está prestado, solo puede cambiar a:PERDIDO, o ser
    // devuelto (DISPONIBLE/EN_REPARACION)
    if (estadoActual == EstadoEjemplar.PRESTADO) {
      if (nuevoEstado != EstadoEjemplar.DISPONIBLE &&
          nuevoEstado != EstadoEjemplar.EN_REPARACION &&
          nuevoEstado != EstadoEjemplar.PERDIDO) {
        throw new BusinessException("Un ejemplar prestado solo puede cambiar a DISPONIBLE, EN_REPARACION o PERDIDO");
      }
    }

    // Si está dado de baja, no puede cambiar de estado
    if (estadoActual == EstadoEjemplar.BAJA) {
      throw new BusinessException("Un ejemplar dado de baja no puede cambiar de estado");
    }
  }

  private Ejemplar getOrThrow(Long id) {
    return ejemplarRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Ejemplar no encontrado con id: " + id));
  }

  private int contarPorEstado(List<Ejemplar> lista, EstadoEjemplar estado) {
    return (int) lista.stream().filter(e -> e.getEstadoEjemplar() == estado).count();
  }

  private void registrarCambioEstado(Ejemplar ejemplar, EstadoEjemplar estadoAnterior,
      EstadoEjemplar estadoNuevo, String motivo) {
    HistorialEstadoEjemplar historial = new HistorialEstadoEjemplar();
    historial.setEjemplar(ejemplar);
    historial.setEstadoAnterior(estadoAnterior);
    historial.setEstadoNuevo(estadoNuevo);
    historial.setMotivo(motivo);
    // Obtener el usuario actual

    Usuario usuario = securityService.getCurrentUser();
    historial.setUsuarioCambio(usuario);

    historialRepository.save(historial);
  }

  private EjemplarResponse mapToResponse(Ejemplar ejemplar) {
    EjemplarResponse response = new EjemplarResponse();
    response.setId_ejemplar(ejemplar.getIdEjemplar());
    response.setCodigoEjemplar(ejemplar.getCodigoEjemplar());
    response.setCodigoTopografico(ejemplar.getCodigoTopografico());
    response.setClasificacionDecimal(
        ejemplar.getClasificacionDecimal());

    response.setCutterAutor(
        ejemplar.getCutterAutor());

    response.setCutterTitulo(
        ejemplar.getCutterTitulo());

    response.setCodigoTopograficoConcat(
        ejemplar.getCodigoTopograficoConcat());
    response.setUbicacionFisica(ejemplar.getUbicacionFisica());
    response.setEstadoEjemplar(ejemplar.getEstadoEjemplar());
    response.setFechaAdquisicion(ejemplar.getFechaAdquisicion());
    response.setObservaciones(ejemplar.getObservaciones());

    // Libro
    if (ejemplar.getEdicion() != null) {
      EdicionSimpleResponse ed = new EdicionSimpleResponse();
      ed.setIdEdicion(ejemplar.getEdicion().getIdEdicion());
      ed.setIsbn(ejemplar.getEdicion().getIsbn());
      ed.setEditorial(ejemplar.getEdicion().getEditorial());
      ed.setAnoPublicacion(ejemplar.getEdicion().getAnoPublicacion());
      ed.setEdicion(ejemplar.getEdicion().getEdicion());
      ed.setImagenPortada(ejemplar.getEdicion().getImagenPortada());
      ed.setPdfUrl(ejemplar.getEdicion().getPdfUrl());
      // ✅ Datos del libro padre dentro de la edición
      if (ejemplar.getEdicion().getLibro() != null) {
        ed.setIdLibro(ejemplar.getEdicion().getLibro().getIdLibro());
        ed.setTitulo(ejemplar.getEdicion().getLibro().getTitulo());
      }

      response.setEdicion(ed);
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
        prestamoResp.setId_prestamo(p.getIdPrestamo());
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
