package com.proyecto.fhce.library.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.users.UsuarioCarreraRequest;
import com.proyecto.fhce.library.dto.response.CarreraSimpleResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioCarreraResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Carrera;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.entities.UsuarioCarrera;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.CarreraRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.repositories.UsuarioCarreraRepository;

@Service
@Transactional
public class UsuarioCarreraService {

  @Autowired
  private UsuarioCarreraRepository usuarioCarreraRepository;

  @Autowired
  private UserRepository usuarioRepository;

  @Autowired
  private CarreraRepository carreraRepository;

  // @Autowired
  // private AuditoriaService auditoriaService;

  public UsuarioCarreraResponse asignarCarrera(UsuarioCarreraRequest request) {
    Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + request.getUsuarioId()));

    Carrera carrera = carreraRepository.findById(request.getCarreraId())
        .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id: " + request.getCarreraId()));

    // Validar que no exista ya la asignación
    if (usuarioCarreraRepository.existsByUsuario_IdUsuarioAndCarrera_IdCarrera(
        request.getUsuarioId(), request.getCarreraId())) {
      throw new DuplicateResourceException("El usuario ya está asignado a esta carrera");
    }

    UsuarioCarrera usuarioCarrera = new UsuarioCarrera();
    usuarioCarrera.setUsuario(usuario);
    usuarioCarrera.setCarrera(carrera);
    usuarioCarrera.setMatricula(request.getMatricula());

    UsuarioCarrera saved = usuarioCarreraRepository.save(usuarioCarrera);

    // auditoriaService.registrar("ASSIGN_CAREER", "users_careers",
    // saved.getId(), null,
    // "Usuario: " + usuario.getUsername() + " - Carrera: " +
    // carrera.getNombre_carrera());

    return mapToResponse(saved);
  }

  public UsuarioCarreraResponse update(Long id, UsuarioCarreraRequest request) {
    UsuarioCarrera usuarioCarrera = usuarioCarreraRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con id: " + id));

    // Validar matrícula única si cambió
    if (request.getMatricula() != null &&
        !request.getMatricula().equals(usuarioCarrera.getMatricula())) {
      // Aquí podrías agregar validación de matrícula única por carrera si lo
      // necesitas
      usuarioCarrera.setMatricula(request.getMatricula());
    }

    // auditoriaService.registrar("UPDATE_ACADEMIC_STATUS", "users_careers",
    // usuarioCarrera.getId(), estadoAnterior, request.getEstadoAcademico());

    UsuarioCarrera updated = usuarioCarreraRepository.save(usuarioCarrera);

    return mapToResponse(updated);
  }

  public void removerCarrera(Long usuarioId, Long carreraId) {
    UsuarioCarrera usuarioCarrera = usuarioCarreraRepository
        .findByUsuario_IdUsuarioAndCarrera_IdCarrera(usuarioId, carreraId)
        .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada"));

    usuarioCarreraRepository.delete(usuarioCarrera);

    // auditoriaService.registrar("REMOVE_CAREER", "users_careers",
    // usuarioCarrera.getId(),
    // "Usuario: " + usuarioCarrera.getUsuario().getUsername() +
    // " - Carrera: " + usuarioCarrera.getCarrera().getNombre_carrera(),
    // null);
  }

  @Transactional(readOnly = true)
  public List<CarreraSimpleResponse> findCarrerasByUsuario(Long usuarioId) {
    Usuario usuario = usuarioRepository.findById(usuarioId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + usuarioId));

    List<UsuarioCarrera> asignaciones = usuarioCarreraRepository
        .findByUsuario_IdUsuario(usuarioId);

    return asignaciones.stream()
        .map(uc -> {
          CarreraSimpleResponse response = new CarreraSimpleResponse();
          response.setId_carrera(uc.getCarrera().getId_carrera());
          response.setNombre_carrera(uc.getCarrera().getNombre_carrera());
          response.setCodigo_carrera(uc.getCarrera().getCodigo_carrera());
          response.setMatricula(uc.getMatricula());
          return response;
        })
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<UsuarioCarreraResponse> findUsuariosByCarrera(Long carreraId) {
    Carrera carrera = carreraRepository.findById(carreraId)
        .orElseThrow(() -> new ResourceNotFoundException("Carrera no encontrada con id: " + carreraId));

    List<UsuarioCarrera> asignaciones = usuarioCarreraRepository
        .findByCarrera_IdCarrera(carreraId);

    return asignaciones.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private UsuarioCarreraResponse mapToResponse(UsuarioCarrera usuarioCarrera) {
    UsuarioCarreraResponse response = new UsuarioCarreraResponse();
    response.setId(usuarioCarrera.getId());
    response.setMatricula(usuarioCarrera.getMatricula());
    response.setFechaAsignacion(usuarioCarrera.getFechaAsignacion());

    // Usuario
    UsuarioSimpleResponse usuarioResp = new UsuarioSimpleResponse();
    usuarioResp.setId_usuario(usuarioCarrera.getUsuario().getId_usuario());
    usuarioResp.setUsername(usuarioCarrera.getUsuario().getUsername());
    usuarioResp.setNombreCompleto(
        usuarioCarrera.getUsuario().getPersona().getNombre() + " " +
            usuarioCarrera.getUsuario().getPersona().getApellido_pat());
    usuarioResp.setEmail(usuarioCarrera.getUsuario().getPersona().getEmail());
    response.setUsuario(usuarioResp);

    // Carrera
    response.setCarrera(mapCarreraToSimple(usuarioCarrera.getCarrera()));

    return response;
  }

  private CarreraSimpleResponse mapCarreraToSimple(Carrera carrera) {
    CarreraSimpleResponse response = new CarreraSimpleResponse();
    response.setId_carrera(carrera.getId_carrera());
    response.setNombre_carrera(carrera.getNombre_carrera());
    response.setCodigo_carrera(carrera.getCodigo_carrera());
    return response;
  }
}
