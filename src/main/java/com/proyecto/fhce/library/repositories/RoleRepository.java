package com.proyecto.fhce.library.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.proyecto.fhce.library.entities.Role;

public interface RoleRepository extends CrudRepository<Role, Long> {
  Optional<Role> findByName(String name);

}
