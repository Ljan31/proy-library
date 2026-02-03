package com.proyecto.fhce.library.security;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.UserRepository;

import org.springframework.security.core.userdetails.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  public CustomUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // Buscar usuario por username
    Usuario usuario = userRepository.findByUsername(username)
        .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

    // Convertir roles a String[]
    // System.out.println(1);

    // System.out.println(
    // usuario.getRoles().stream()
    // .map(Role::getName)
    // .collect(Collectors.toList()));

    String[] roles = usuario.getRoles().stream()
        .map(role -> role.getName().replace("ROLE_", "")) // quitar prefijo si lo tiene
        .toArray(String[]::new);

    // Crear UserDetails de Spring Security
    return User.builder()
        .username(usuario.getUsername())
        .password(usuario.getPassword()) // debe estar codificada con BCrypt
        .roles(roles)
        .build();

    // return UserDetailsImpl.build(usuario);
  }
}
