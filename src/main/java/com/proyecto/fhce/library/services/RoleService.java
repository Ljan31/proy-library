package com.proyecto.fhce.library.services;

import java.util.List;
import java.util.Optional;

import com.proyecto.fhce.library.entities.Role;

public interface RoleService {
  List<Role> findAll();

  Optional<Role> findById(Long id);

  Optional<Role> findByName(String rol);

  Role save(Role rol);

  Optional<Role> update(Role rol, Long id);

  void delete(Long id);
}
