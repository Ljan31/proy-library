package com.proyecto.fhce.library.services;

import java.util.List;
import java.util.Map;

import com.proyecto.fhce.library.dto.request.PermisoRequest;
import com.proyecto.fhce.library.dto.response.PermisoResponse;

public interface PermisoService {
  PermisoResponse create(PermisoRequest request);

  PermisoResponse update(Long id, PermisoRequest request);

  void delete(Long id);

  List<PermisoResponse> findAll();

  PermisoResponse findById(Long id);

  PermisoResponse findByNombre(String nombre);

  List<PermisoResponse> findByModulo(String modulo);

  List<String> findModulos();

  List<PermisoResponse> findByRoleId(Long roleId);

  Map<String, List<PermisoResponse>> findAllGroupedByModulo();
}
