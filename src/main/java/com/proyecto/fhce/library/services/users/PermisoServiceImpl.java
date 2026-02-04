package com.proyecto.fhce.library.services.users;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.PermisoRequest;
import com.proyecto.fhce.library.dto.response.PermisoResponse;
import com.proyecto.fhce.library.entities.Permiso;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.PermisoRepository;

@Service
public class PermisoServiceImpl implements PermisoService {

  @Autowired
  private PermisoRepository permisoRepository;

  // @Autowired
  // private AuditoriaService auditoriaService;
  @Transactional
  public PermisoResponse create(PermisoRequest request) {
    if (permisoRepository.existsByNombrePermiso(request.getNombre_permiso())) {
      throw new DuplicateResourceException("Ya existe un permiso con nombre: " +
          request.getNombre_permiso());
    }

    Permiso permiso = new Permiso();
    permiso.setNombre_permiso(request.getNombre_permiso());
    permiso.setDescripcion(request.getDescripcion());
    permiso.setModulo(request.getModulo());

    Permiso saved = permisoRepository.save(permiso);

    // auditoriaService.registrar("CREATE_PERMISSION", "permissions",
    // saved.getId_permiso(), null, saved.getNombre_permiso());

    return mapToResponse(saved);
  }

  @Transactional
  public PermisoResponse update(Long id, PermisoRequest request) {
    Permiso permiso = permisoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + id));

    // Validar nombre único si cambió
    if (!permiso.getNombre_permiso().equals(request.getNombre_permiso()) &&
        permisoRepository.existsByNombrePermiso(request.getNombre_permiso())) {
      throw new DuplicateResourceException("Ya existe un permiso con nombre: " +
          request.getNombre_permiso());
    }

    // String nombreAnterior = permiso.getNombre_permiso();

    permiso.setNombre_permiso(request.getNombre_permiso());
    permiso.setDescripcion(request.getDescripcion());
    permiso.setModulo(request.getModulo());

    Permiso updated = permisoRepository.save(permiso);

    // auditoriaService.registrar("UPDATE_PERMISSION", "permissions",
    // updated.getId_permiso(), nombreAnterior, updated.getNombre_permiso());

    return mapToResponse(updated);
  }

  @Transactional
  public void delete(Long id) {
    Permiso permiso = permisoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + id));

    // Validar que no esté asignado a ningún rol
    if (permiso.getRoles() != null && !permiso.getRoles().isEmpty()) {
      throw new BusinessException("No se puede eliminar el permiso porque está asignado a " +
          permiso.getRoles().size() + " rol(es)");
    }

    permisoRepository.delete(permiso);

    // auditoriaService.registrar("DELETE_PERMISSION", "permissions",
    // permiso.getId_permiso(), permiso.getNombre_permiso(), null);
  }

  @Transactional(readOnly = true)
  public List<PermisoResponse> findAll() {
    return permisoRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public PermisoResponse findById(Long id) {
    Permiso permiso = permisoRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con id: " + id));
    return mapToResponse(permiso);
  }

  @Transactional(readOnly = true)
  public PermisoResponse findByNombre(String nombre) {
    Permiso permiso = permisoRepository.findByNombrePermiso(nombre)
        .orElseThrow(() -> new ResourceNotFoundException("Permiso no encontrado con nombre: " + nombre));
    return mapToResponse(permiso);
  }

  @Transactional(readOnly = true)
  public List<PermisoResponse> findByModulo(String modulo) {
    return permisoRepository.findByModulo(modulo).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<String> findModulos() {
    return permisoRepository.findDistinctModulos();
  }

  @Transactional(readOnly = true)
  public List<PermisoResponse> findByRoleId(Long roleId) {
    return permisoRepository.findByRoleId(roleId).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public Map<String, List<PermisoResponse>> findAllGroupedByModulo() {
    List<Permiso> permisos = permisoRepository.findAll();

    return permisos.stream()
        .collect(Collectors.groupingBy(
            p -> p.getModulo() != null ? p.getModulo() : "SIN_MODULO",
            Collectors.mapping(this::mapToResponse, Collectors.toList())));
  }

  private PermisoResponse mapToResponse(Permiso permiso) {
    PermisoResponse response = new PermisoResponse();
    response.setId_permiso(permiso.getId_permiso());
    response.setNombre_permiso(permiso.getNombre_permiso());
    response.setDescripcion(permiso.getDescripcion());
    response.setModulo(permiso.getModulo());
    return response;
  }
}