package com.proyecto.fhce.library.repositories;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import com.proyecto.fhce.library.entities.Usuario;

public interface UserRepository extends CrudRepository<Usuario, Long> {
  boolean existsByUsername(String username);

  Optional<Usuario> findByUsername(String username);
}
