package com.proyecto.fhce.library.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.AsignarEncargadoRequest;
import com.proyecto.fhce.library.dto.response.BibliotecaEncargadoResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.BibliotecaEncargado;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.RolEncargado;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaEncargadoRepository;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
public class BibliotecaEncargadoService {

  @Autowired
  private BibliotecaEncargadoRepository encargadoRepository;
  @Autowired
  private BibliotecaRepository bibliotecaRepository;
  @Autowired
  private UserRepository usuarioRepository;
  @Autowired
  private RoleRepository roleRepository;

  @Transactional
  public BibliotecaEncargadoResponse asignarEncargado(Long bibliotecaId, AsignarEncargadoRequest request) {
    Biblioteca biblioteca = bibliotecaRepository.findById(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

    Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    // Validar que el usuario tenga rol apropiado
    boolean tieneRol = usuario.getRoles().stream()
        .anyMatch(r -> r.getName().equals("ROLE_BIBLIOTECARIO") || r.getName().equals("ROLE_ADMIN"));
    if (!tieneRol) {
      throw new BusinessException("El usuario debe tener rol BIBLIOTECARIO o ADMIN");
    }

    // Evitar duplicado activo
    if (encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(
        bibliotecaId, request.getUsuarioId())) {
      throw new DuplicateResourceException("El usuario ya es encargado activo de esta biblioteca");
    }

    // Si se asigna PRINCIPAL, verificar que no haya otro principal activo
    if (request.getRolEncargado() == RolEncargado.PRINCIPAL) {
      encargadoRepository.findEncargadoPrincipal(bibliotecaId, RolEncargado.PRINCIPAL)
          .ifPresent(actual -> {
            throw new BusinessException(
                "Ya existe un encargado PRINCIPAL. Desasígnalo antes de asignar uno nuevo.");
          });
    }

    BibliotecaEncargado encargado = new BibliotecaEncargado();
    encargado.setBiblioteca(biblioteca);
    encargado.setUsuario(usuario);
    encargado.setRolEncargado(request.getRolEncargado());
    encargado.setFechaAsignacion(LocalDateTime.now());
    encargado.setActivo(true);

    return mapToResponse(encargadoRepository.save(encargado));
  }

  @Transactional
  public void removerEncargado(Long bibliotecaId, Long usuarioId) {
    BibliotecaEncargado encargado = encargadoRepository
        .findByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(bibliotecaId, usuarioId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "El usuario no es encargado activo de esta biblioteca"));

    // Soft delete: desactivar, no borrar
    Usuario usuario = encargado.getUsuario();
    // 1. Desactivar encargado
    encargadoRepository.desactivarEncargado(bibliotecaId, usuarioId, LocalDateTime.now());

    // 2. Verificar si era AUXILIAR
    boolean eraAuxiliar = encargado.getRolEncargado() == RolEncargado.AUXILIAR;

    if (eraAuxiliar) {

      // 3. Verificar si aún tiene otras bibliotecas activas
      boolean tieneOtrosEncargos = encargadoRepository
          .existsByUsuario_IdUsuarioAndActivoTrue(usuarioId);

      // 4. Si ya no tiene ninguno → quitar ROLE_AUXILIAR
      if (!tieneOtrosEncargos) {

        Role rolAuxiliar = roleRepository.findByName("ROLE_AUXILIAR")
            .orElseThrow(() -> new ResourceNotFoundException("Rol AUXILIAR no encontrado"));

        usuario.getRoles().removeIf(r -> r.getName().equals("ROLE_AUXILIAR"));

        usuarioRepository.save(usuario);
      }
    }
  }

  @Transactional(readOnly = true)
  public List<BibliotecaEncargadoResponse> findEncargadosByBiblioteca(Long bibliotecaId) {
    if (!bibliotecaRepository.existsById(bibliotecaId)) {
      throw new ResourceNotFoundException("Biblioteca no encontrada");
    }
    return encargadoRepository.findEncargadosActivosConDetalle(bibliotecaId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<BibliotecaEncargadoResponse> findBibliotecasByEncargado(Long usuarioId) {
    return encargadoRepository.findByUsuario_IdUsuarioAndActivoTrue(usuarioId)
        .stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private BibliotecaEncargadoResponse mapToResponse(BibliotecaEncargado be) {
    BibliotecaEncargadoResponse response = new BibliotecaEncargadoResponse();
    response.setId(be.getId());
    response.setRolEncargado(be.getRolEncargado());
    response.setFechaAsignacion(be.getFechaAsignacion());
    response.setActivo(be.getActivo());

    UsuarioSimpleResponse u = new UsuarioSimpleResponse();
    u.setId_usuario(be.getUsuario().getId_usuario());
    u.setUsername(be.getUsuario().getUsername());
    u.setNombreCompleto(
        be.getUsuario().getPersona().getNombre() + " " +
            be.getUsuario().getPersona().getApellido_pat());
    response.setUsuario(u);

    return response;
  }
}