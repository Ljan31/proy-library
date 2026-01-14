package com.proyecto.fhce.library.services;

import java.util.List;
import java.util.Optional;

import com.proyecto.fhce.library.entities.Usuario;

public interface UserService {
  List<Usuario> findAll();

  Optional<Usuario> findById(Long id);

  Optional<Usuario> findByUsername(String username);

  Usuario save(Usuario user);

  Optional<Usuario> update(Usuario user, Long id);

  void delete(Long id);

  boolean existsByUsername(String username);

}
