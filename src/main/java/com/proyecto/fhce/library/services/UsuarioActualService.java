package com.proyecto.fhce.library.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.UserRepository;

/**
 * Resuelve el usuario autenticado a partir del SecurityContext (JWT).
 * Único lugar donde se hace este trabajo, para no repetir el bloque en cada
 * service/controller.
 */
@Component
public class UsuarioActualService {

  private final UserRepository usuarioRepository;

  public UsuarioActualService(UserRepository usuarioRepository) {
    this.usuarioRepository = usuarioRepository;
  }

  /**
   * Id del usuario autenticado. Lanza AccessDeniedException (403) si no hay
   * sesión válida.
   */
  public Long idUsuarioActual() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserDetails userDetails)) {
      throw new AccessDeniedException("Usuario no autenticado");
    }
    return usuarioRepository.findByUsername(userDetails.getUsername())
        .map(Usuario::getId_usuario)
        .orElseThrow(() -> new AccessDeniedException("Usuario no encontrado"));
  }
}