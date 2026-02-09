package com.proyecto.fhce.library.services.users;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.users.RoleRequest;
import com.proyecto.fhce.library.dto.response.PermisoResponse;
import com.proyecto.fhce.library.dto.response.users.RoleResponse;
import com.proyecto.fhce.library.entities.Permiso;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.PermisoRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
public class RoleServiceImpl implements RoleService {

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PermisoRepository permisoRepository;

  @Autowired
  private UserRepository usuarioRepository;

  @Transactional(readOnly = true)
  public List<RoleResponse> findAll() {
    return roleRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public RoleResponse findById(Long id) {
    Role role = roleRepository.findByIdWithPermisos(id)
        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));
    return mapToResponse(role);
  }

  @Transactional(readOnly = true)
  public RoleResponse findByName(String name) {
    Role role = roleRepository.findByName(name)
        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con nombre: " + name));
    return mapToResponse(role);
  }

  public RoleResponse create(RoleRequest request) {
    if (roleRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException("Ya existe un rol con nombre: " + request.getName());
    }

    Role role = new Role();
    role.setName(request.getName());

    // Asignar permisos si fueron proporcionados
    if (request.getPermisoIds() != null && !request.getPermisoIds().isEmpty()) {
      Set<Permiso> permisos = request.getPermisoIds().stream()
          .map(permisoId -> permisoRepository.findById(permisoId)
              .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + permisoId)))
          .collect(Collectors.toSet());
      role.setPermisos(permisos);
    }

    Role saved = roleRepository.save(role);

    // auditoriaService.registrar("CREATE_ROLE", "roles",
    // saved.getId_role(), null, saved.getName());

    return mapToResponse(saved);
  }

  // @Override
  // public Optional<Role> update(Role rol, Long id) {

  // Optional<Role> roleOptional = this.findById(id);
  // return roleOptional.map(existing -> {
  // if (rol.getName() != "")
  // existing.setName(rol.getName());
  // return Optional.of(roleRepository.save(existing));
  // }).orElseGet(() -> Optional.empty());
  // }

  public void delete(Long id) {
    Role role = roleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));

    // Validar que no haya usuarios con este rol
    Long usuariosCount = usuarioRepository.countByRoleName(role.getName());
    if (usuariosCount > 0) {
      throw new BusinessException("No se puede eliminar el rol porque tiene " +
          usuariosCount + " usuarios asignados");
    }

    roleRepository.delete(role);

    // auditoriaService.registrar("DELETE_ROLE", "roles",
    // role.getId_role(), role.getName(), null);
  }

  public RoleResponse update(RoleRequest request, Long id) {
    Role role = roleRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + id));

    // Validar nombre único si cambió
    if (!role.getName().equals(request.getName()) &&
        roleRepository.existsByName(request.getName())) {
      throw new DuplicateResourceException("Ya existe un rol con nombre: " + request.getName());
    }

    // String nombreAnterior = role.getName();

    role.setName(request.getName());

    // Actualizar permisos
    if (request.getPermisoIds() != null) {
      Set<Permiso> permisos = request.getPermisoIds().stream()
          .map(permisoId -> permisoRepository.findById(permisoId)
              .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + permisoId)))
          .collect(Collectors.toSet());
      role.setPermisos(permisos);
    }

    Role updated = roleRepository.save(role);

    // auditoriaService.registrar("UPDATE_ROLE", "roles",
    // updated.getId_role(), nombreAnterior, updated.getName());

    return mapToResponse(updated);
  }

  @Transactional(readOnly = true)
  public List<RoleResponse> findAllWithPermisos() {
    return roleRepository.findAllWithPermisos().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public RoleResponse asignarPermiso(Long roleId, Long permisoId) {
    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + roleId));

    Permiso permiso = permisoRepository.findById(permisoId)
        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + permisoId));

    if (role.getPermisos().contains(permiso)) {
      throw new BusinessException("El rol ya tiene asignado este permiso");
    }

    role.getPermisos().add(permiso);
    Role updated = roleRepository.save(role);

    // auditoriaService.registrar("ASSIGN_PERMISSION_TO_ROLE", "roles_permissions",
    // roleId, null, "Permiso: " + permiso.getNombre_permiso());

    return mapToResponse(updated);
  }

  public RoleResponse removerPermiso(Long roleId, Long permisoId) {
    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con id: " + roleId));

    Permiso permiso = permisoRepository.findById(permisoId)
        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + permisoId));

    if (!role.getPermisos().contains(permiso)) {
      throw new BusinessException("El rol no tiene asignado este permiso");
    }

    role.getPermisos().remove(permiso);
    Role updated = roleRepository.save(role);

    // auditoriaService.registrar("REMOVE_PERMISSION_FROM_ROLE",
    // "roles_permissions",
    // roleId, "Permiso: " + permiso.getNombre_permiso(), null);

    return mapToResponse(updated);
  }

  private RoleResponse mapToResponse(Role role) {
    RoleResponse response = new RoleResponse();
    response.setId_role(role.getId_role());
    response.setName(role.getName());

    // Mapear permisos
    if (role.getPermisos() != null) {
      Set<PermisoResponse> permisos = role.getPermisos().stream()
          .map(this::mapPermisoToResponse)
          .collect(Collectors.toSet());
      response.setPermisos(permisos);
    }

    // Contar usuarios con este rol
    if (role.getUsers() != null) {
      response.setUsuariosCount(role.getUsers().size());
    } else {
      // Si no está cargada la relación, hacer query
      Long count = usuarioRepository.countByRoleName(role.getName());
      response.setUsuariosCount(count != null ? count.intValue() : 0);
    }

    return response;
  }

  private PermisoResponse mapPermisoToResponse(Permiso permiso) {
    PermisoResponse response = new PermisoResponse();
    response.setId_permiso(permiso.getId_permiso());
    response.setNombre_permiso(permiso.getNombre_permiso());
    response.setDescripcion(permiso.getDescripcion());
    response.setModulo(permiso.getModulo());
    return response;
  }
}
