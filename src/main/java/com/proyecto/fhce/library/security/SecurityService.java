package com.proyecto.fhce.library.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.services.users.UserService;

@Component("securityService")
public class SecurityService {

  private final UserService userService;

  public SecurityService(UserService userService) {
    this.userService = userService;
  }

  /**
   * Devuelve true si el usuario autenticado es dueño de la persona con id_persona
   */
  public boolean isOwner(Long idPersona, Authentication authentication) {
    String username = authentication.getName();
    Usuario usuario = userService.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    return usuario.getPersona().getId_persona().equals(idPersona);
  }

  /**
   * Devuelve true si el usuario autenticado es dueño del usuario con id_usuario
   */
  public boolean isOwnerUser(Long idUsuario, Authentication authentication) {
    // String username = authentication.getName();
    // Usuario usuario = userService.findByUsername(username)
    // .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    // return usuario.getId_usuario().equals(idUsuario);
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    return userDetails.getId().equals(idUsuario);
  }
}
