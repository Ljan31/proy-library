package com.proyecto.fhce.library.services.users;

import java.util.List;

import com.proyecto.fhce.library.dto.request.users.RoleRequest;
import com.proyecto.fhce.library.dto.response.users.RoleResponse;

public interface RoleService {
  List<RoleResponse> findAll();

  RoleResponse findById(Long id);

  RoleResponse findByName(String rol);

  RoleResponse create(RoleRequest request);

  RoleResponse update(RoleRequest request, Long id);

  void delete(Long id);

  List<RoleResponse> findAllWithPermisos();

  RoleResponse asignarPermiso(Long roleId, Long permisoId);

  RoleResponse removerPermiso(Long roleId, Long permisoId);
}
