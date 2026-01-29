package com.proyecto.fhce.library.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.fhce.library.entities.Usuario;

public interface UserRepository extends JpaRepository<Usuario, Long> {
  boolean existsByUsername(String username);

  Optional<Usuario> findByUsername(String username);
}
