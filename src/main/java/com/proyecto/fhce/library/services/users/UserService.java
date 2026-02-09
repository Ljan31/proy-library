package com.proyecto.fhce.library.services.users;

import java.util.List;
import java.util.Optional;

import com.proyecto.fhce.library.dto.request.users.ChangePasswordRequest;
import com.proyecto.fhce.library.dto.request.users.RegisterRequest;
import com.proyecto.fhce.library.dto.request.users.UsuarioUpdateRequest;
import com.proyecto.fhce.library.dto.response.users.UsuarioResponse;
import com.proyecto.fhce.library.entities.Usuario;

public interface UserService {

  UsuarioResponse create(RegisterRequest request);

  UsuarioResponse update(Long id, UsuarioUpdateRequest request);

  void changePassword(String username, ChangePasswordRequest request);

  void toggleEnabled(Long id);

  List<UsuarioResponse> findAll();

  UsuarioResponse findById(Long id);

  List<UsuarioResponse> findByRoleName(String roleName);

  List<UsuarioResponse> search(String searchTerm);

  Optional<Usuario> findByUsername(String username);

  // Usuario save(Usuario user);

  // Optional<Usuario> update(Usuario user, Long id);

  // void delete(Long id);

  // boolean existsByUsername(String username);

}
