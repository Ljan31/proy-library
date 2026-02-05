package com.proyecto.fhce.library.services.library;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.BibliotecaRequest;
import com.proyecto.fhce.library.dto.response.BibliotecaResponse;
import com.proyecto.fhce.library.dto.response.BibliotecaSimpleResponse;
import com.proyecto.fhce.library.dto.response.CarreraSimpleResponse;
import com.proyecto.fhce.library.dto.response.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.Carrera;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.CarreraRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
public class BibliotecaServiceImpl implements BibliotecaService {
  @Autowired
  private BibliotecaRepository bibliotecaRepository;

  @Autowired
  private CarreraRepository carreraRepository;

  @Autowired
  private UserRepository usuarioRepository;

  // @Autowired
  // private EjemplarRepository ejemplarRepository;

  // @Autowired
  // private PrestamoRepository prestamoRepository;

  // @Autowired
  // private ReservaRepository reservaRepository;

  // @Autowired
  // private AuditoriaService auditoriaService;

  @Transactional
  public BibliotecaResponse create(BibliotecaRequest request) {
    // Validar nombre único
    if (bibliotecaRepository.existsByNombre(request.getNombre())) {
      throw new DuplicateResourceException("Ya existe una biblioteca con nombre: " +
          request.getNombre());
    }

    Biblioteca biblioteca = new Biblioteca();
    biblioteca.setNombre(request.getNombre());
    biblioteca.setTipoBiblioteca(request.getTipoBiblioteca());
    biblioteca.setDireccion(request.getDireccion());
    biblioteca.setTelefono(request.getTelefono());
    biblioteca.setEmail(request.getEmail());
    biblioteca.setHorario_atencion(request.getHorario_atencion());
    biblioteca.setEstado(request.getEstado() != null ? request.getEstado() : EstadoBiblioteca.ACTIVA);

    // Asignar carrera si es biblioteca de carrera
    if (request.getTipoBiblioteca() == TipoBiblioteca.CARRERA) {
      if (request.getCarreraId() == null) {
        throw new BusinessException("Una biblioteca de carrera debe tener una carrera asignada");
      }

      Carrera carrera = carreraRepository.findById(request.getCarreraId())
          .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada"));

      // Validar que no exista ya una biblioteca para esta carrera
      Optional<Biblioteca> existente = bibliotecaRepository
          .findByCarrera_IdCarreraAndTipoBiblioteca(request.getCarreraId(), TipoBiblioteca.CARRERA);
      if (existente.isPresent()) {
        throw new BusinessException("Ya existe una biblioteca de carrera para: " +
            carrera.getNombre_carrera());
      }

      biblioteca.setCarrera(carrera);
    } else if (request.getTipoBiblioteca() == TipoBiblioteca.FACULTATIVA) {
      // Validar que no exista ya una biblioteca facultativa
      Optional<Biblioteca> facultativa = bibliotecaRepository.findBibliotecaFacultativa();
      if (facultativa.isPresent()) {
        throw new BusinessException("Ya existe una biblioteca facultativa");
      }
    }

    // Asignar encargado
    if (request.getEncargadoId() != null) {
      Usuario encargado = usuarioRepository.findById(request.getEncargadoId())
          .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado"));
      biblioteca.setEncargado(encargado);
    }

    Biblioteca saved = bibliotecaRepository.save(biblioteca);

    // auditoriaService.registrar("CREATE_LIBRARY", "libraries",
    // saved.getId_biblioteca(), null, saved.getNombre());

    return mapToResponse(saved);
  }

  @Transactional
  public BibliotecaResponse update(Long id, BibliotecaRequest request) {
    Biblioteca biblioteca = bibliotecaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada con id: " + id));

    // Validar nombre único si cambió
    if (!biblioteca.getNombre().equals(request.getNombre()) &&
        bibliotecaRepository.existsByNombre(request.getNombre())) {
      throw new DuplicateResourceException("Ya existe una biblioteca con nombre: " +
          request.getNombre());
    }

    String nombreAnterior = biblioteca.getNombre();

    biblioteca.setNombre(request.getNombre());
    biblioteca.setTipoBiblioteca(request.getTipoBiblioteca());
    biblioteca.setDireccion(request.getDireccion());
    biblioteca.setTelefono(request.getTelefono());
    biblioteca.setEmail(request.getEmail());
    biblioteca.setHorario_atencion(request.getHorario_atencion());

    if (request.getEstado() != null) {
      biblioteca.setEstado(request.getEstado());
    }

    // Actualizar carrera
    if (request.getCarreraId() != null) {
      Carrera carrera = carreraRepository.findById(request.getCarreraId())
          .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada"));
      biblioteca.setCarrera(carrera);
    }

    // Actualizar encargado
    if (request.getEncargadoId() != null) {
      Usuario encargado = usuarioRepository.findById(request.getEncargadoId())
          .orElseThrow(() -> new ResourceNotFoundException("Encargado no encontrado"));
      biblioteca.setEncargado(encargado);
    }

    Biblioteca updated = bibliotecaRepository.save(biblioteca);

    // auditoriaService.registrar("UPDATE_LIBRARY", "libraries",
    // updated.getId_biblioteca(), nombreAnterior, updated.getNombre());

    return mapToResponse(updated);
  }

  @Transactional
  public void delete(Long id) {
    Biblioteca biblioteca = bibliotecaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada con id: " + id));

    // Validar que no tenga ejemplares
    // Long ejemplaresCount = ejemplarRepository.countByBibliotecaAndEstado(id,
    // null);
    // if (ejemplaresCount != null && ejemplaresCount > 0) {
    // throw new BusinessException("No se puede eliminar la biblioteca porque tiene
    // " +
    // ejemplaresCount + " ejemplar(es) registrado(s)");
    // }

    bibliotecaRepository.delete(biblioteca);

    // auditoriaService.registrar("DELETE_LIBRARY", "libraries",
    // biblioteca.getId_biblioteca(), biblioteca.getNombre(), null);
  }

  @Transactional
  public void cambiarEstado(Long id, EstadoBiblioteca nuevoEstado) {
    Biblioteca biblioteca = bibliotecaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada con id: " + id));

    EstadoBiblioteca estadoAnterior = biblioteca.getEstado();
    biblioteca.setEstado(nuevoEstado);

    bibliotecaRepository.save(biblioteca);

    // auditoriaService.registrar("CHANGE_LIBRARY_STATUS", "libraries",
    // biblioteca.getId_biblioteca(), estadoAnterior, nuevoEstado);
  }

  @Transactional
  public void asignarEncargado(Long bibliotecaId, Long usuarioId) {
    Biblioteca biblioteca = bibliotecaRepository.findById(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

    Usuario encargado = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    // Verificar que el usuario tenga rol de bibliotecario
    boolean esBibliotecario = encargado.getRoles().stream()
        .anyMatch(role -> role.getName().equals("ROLE_BIBLIOTECARIO") ||
            role.getName().equals("ROLE_ADMIN"));

    if (!esBibliotecario) {
      throw new BusinessException("El usuario debe tener rol de BIBLIOTECARIO o ADMIN");
    }

    biblioteca.setEncargado(encargado);
    bibliotecaRepository.save(biblioteca);

    // auditoriaService.registrar("ASSIGN_LIBRARY_MANAGER", "libraries",
    // biblioteca.getId_biblioteca(), null, "Encargado: " +
    // encargado.getUsername());
  }

  @Transactional(readOnly = true)
  public List<BibliotecaResponse> findAll() {
    return bibliotecaRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BibliotecaResponse> findActivas() {
    return bibliotecaRepository.findByEstadoOrdered(EstadoBiblioteca.ACTIVA).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public BibliotecaResponse findById(Long id) {
    Biblioteca biblioteca = bibliotecaRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada con id: " + id));
    return mapToResponse(biblioteca);
  }

  @Transactional(readOnly = true)
  public List<BibliotecaResponse> findByTipo(TipoBiblioteca tipo) {
    return bibliotecaRepository.findByTipoBiblioteca(tipo).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public BibliotecaResponse findBibliotecaFacultativa() {
    Biblioteca biblioteca = bibliotecaRepository.findBibliotecaFacultativa()
        .orElseThrow(() -> new ResourceNotFoundException("No existe biblioteca facultativa"));
    return mapToResponse(biblioteca);
  }

  @Transactional(readOnly = true)
  public List<BibliotecaResponse> findByCarrera(Long carreraId) {
    return bibliotecaRepository.findByCarrera_IdCarrera(carreraId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BibliotecaResponse> search(String searchTerm) {
    return bibliotecaRepository.searchBibliotecas(searchTerm).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  // @Transactional(readOnly = true)
  // public EstadisticasBibliotecaResponse obtenerEstadisticas(Long id) {
  // Biblioteca biblioteca = bibliotecaRepository.findById(id)
  // .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada
  // con id: " + id));

  // EstadisticasBibliotecaResponse stats = new EstadisticasBibliotecaResponse();

  // BibliotecaSimpleResponse biblioResp = new BibliotecaSimpleResponse();
  // biblioResp.setId_biblioteca(biblioteca.getId_biblioteca());
  // biblioResp.setNombre(biblioteca.getNombre());
  // biblioResp.setTipoBiblioteca(biblioteca.getTipoBiblioteca());
  // stats.setBiblioteca(biblioResp);

  // // Obtener ejemplares
  // List<Ejemplar> ejemplares =
  // ejemplarRepository.findByBiblioteca_IdBiblioteca(id);
  // stats.setTotalEjemplares(ejemplares.size());

  // long disponibles = ejemplares.stream()
  // .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.DISPONIBLE)
  // .count();
  // stats.setEjemplaresDisponibles((int) disponibles);

  // long prestados = ejemplares.stream()
  // .filter(e -> e.getEstadoEjemplar() == EstadoEjemplar.PRESTADO)
  // .count();
  // stats.setEjemplaresPrestados((int) prestados);

  // // Contar libros únicos
  // long librosUnicos = ejemplares.stream()
  // .map(e -> e.getLibro().getId_libro())
  // .distinct()
  // .count();
  // stats.setTotalLibros((int) librosUnicos);

  // // Préstamos activos
  // List<Prestamo> prestamosActivos = prestamoRepository
  // .findByBiblioteca_IdBibliotecaAndEstadoPrestamo(id, EstadoPrestamo.ACTIVO);
  // stats.setPrestamosActivos(prestamosActivos.size());

  // // Préstamos vencidos
  // List<Prestamo> vencidos =
  // prestamoRepository.findPrestamosVencidos(LocalDate.now())
  // .stream()
  // .filter(p -> p.getBiblioteca().getId_biblioteca().equals(id))
  // .collect(Collectors.toList());
  // stats.setPrestamosVencidos(vencidos.size());

  // // Reservas pendientes
  // List<Reserva> reservas = reservaRepository
  // .findByBiblioteca_IdBibliotecaAndEstadoReserva(id, EstadoReserva.PENDIENTE);
  // stats.setReservasPendientes(reservas != null ? reservas.size() : 0);

  // // Usuarios activos (usuarios con préstamos en esta biblioteca)
  // Long usuariosActivos =
  // prestamoRepository.countUsuariosActivosByBiblioteca(id);
  // stats.setUsuariosActivos(usuariosActivos != null ? usuariosActivos.intValue()
  // : 0);

  // // Libros más prestados
  // List<Object[]> librosMasPrestados = prestamoRepository
  // .findLibrosMasPrestadosByBiblioteca(id);

  // List<LibroMasPrestadoResponse> topLibros = librosMasPrestados.stream()
  // .limit(10)
  // .map(arr -> {
  // LibroMasPrestadoResponse libro = new LibroMasPrestadoResponse();
  // libro.setTitulo((String) arr[0]);
  // libro.setCantidadPrestamos((Long) arr[1]);
  // return libro;
  // })
  // .collect(Collectors.toList());
  // stats.setLibrosMasPrestados(topLibros);

  // return stats;
  // }

  // @Transactional(readOnly = true)
  // public InventarioBibliotecaResponse obtenerInventario(Long id) {
  // Biblioteca biblioteca = bibliotecaRepository.findById(id)
  // .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no
  // encontrada"));

  // InventarioBibliotecaResponse inventario = new InventarioBibliotecaResponse();
  // inventario.setBiblioteca(mapToSimpleResponse(biblioteca));

  // // Contar ejemplares por estado
  // Long disponibles = ejemplarRepository.countByBibliotecaAndEstado(id,
  // EstadoEjemplar.DISPONIBLE);
  // Long prestados = ejemplarRepository.countByBibliotecaAndEstado(id,
  // EstadoEjemplar.PRESTADO);
  // Long reservados = ejemplarRepository.countByBibliotecaAndEstado(id,
  // EstadoEjemplar.RESERVADO);
  // Long enReparacion = ejemplarRepository.countByBibliotecaAndEstado(id,
  // EstadoEjemplar.EN_REPARACION);
  // Long baja = ejemplarRepository.countByBibliotecaAndEstado(id,
  // EstadoEjemplar.BAJA);
  // Long perdidos = ejemplarRepository.countByBibliotecaAndEstado(id,
  // EstadoEjemplar.PERDIDO);

  // inventario.setDisponibles(disponibles != null ? disponibles.intValue() : 0);
  // inventario.setPrestados(prestados != null ? prestados.intValue() : 0);
  // inventario.setReservados(reservados != null ? reservados.intValue() : 0);
  // inventario.setEnReparacion(enReparacion != null ? enReparacion.intValue() :
  // 0);
  // inventario.setBaja(baja != null ? baja.intValue() : 0);
  // inventario.setPerdidos(perdidos != null ? perdidos.intValue() : 0);
  // inventario.setTotal(
  // inventario.getDisponibles() + inventario.getPrestados() +
  // inventario.getReservados() + inventario.getEnReparacion() +
  // inventario.getBaja() + inventario.getPerdidos()
  // );

  // return inventario;
  // }

  private BibliotecaResponse mapToResponse(Biblioteca biblioteca) {
    BibliotecaResponse response = new BibliotecaResponse();
    response.setId_biblioteca(biblioteca.getId_biblioteca());
    response.setNombre(biblioteca.getNombre());
    response.setTipoBiblioteca(biblioteca.getTipoBiblioteca());
    response.setDireccion(biblioteca.getDireccion());
    response.setTelefono(biblioteca.getTelefono());
    response.setEmail(biblioteca.getEmail());
    response.setHorario_atencion(biblioteca.getHorario_atencion());
    response.setEstado(biblioteca.getEstado());

    // Carrera
    if (biblioteca.getCarrera() != null) {
      CarreraSimpleResponse carrera = new CarreraSimpleResponse();
      carrera.setId_carrera(biblioteca.getCarrera().getId_carrera());
      carrera.setNombre_carrera(biblioteca.getCarrera().getNombre_carrera());
      carrera.setCodigo_carrera(biblioteca.getCarrera().getCodigo_carrera());
      response.setCarrera(carrera);
    }

    // Encargado
    if (biblioteca.getEncargado() != null) {
      UsuarioSimpleResponse encargado = new UsuarioSimpleResponse();
      encargado.setId_usuario(biblioteca.getEncargado().getId_usuario());
      encargado.setUsername(biblioteca.getEncargado().getUsername());
      encargado.setNombreCompleto(
          biblioteca.getEncargado().getPersona().getNombre() + " " +
              biblioteca.getEncargado().getPersona().getApellido_pat());
      response.setEncargado(encargado);
    }

    // Calcular estadísticas básicas
    // Long total = ejemplarRepository.countByBibliotecaAndEstado(
    // biblioteca.getId_biblioteca(), null);
    // response.setEjemplaresTotal(total != null ? total.intValue() : 0);

    // Long disponibles = ejemplarRepository.countByBibliotecaAndEstado(
    // biblioteca.getId_biblioteca(), EstadoEjemplar.DISPONIBLE);
    // response.setEjemplaresDisponibles(disponibles != null ?
    // disponibles.intValue() : 0);

    return response;
  }

  private BibliotecaSimpleResponse mapToSimpleResponse(Biblioteca biblioteca) {
    BibliotecaSimpleResponse response = new BibliotecaSimpleResponse();
    response.setId_biblioteca(biblioteca.getId_biblioteca());
    response.setNombre(biblioteca.getNombre());
    response.setTipoBiblioteca(biblioteca.getTipoBiblioteca());
    return response;
  }
}
