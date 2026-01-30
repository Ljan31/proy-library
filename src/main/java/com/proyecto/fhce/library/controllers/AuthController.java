package com.proyecto.fhce.library.controllers;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.LoginRequest;
import com.proyecto.fhce.library.dto.request.RegisterRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.LoginResponse;
import com.proyecto.fhce.library.dto.response.PersonaResponse;
import com.proyecto.fhce.library.dto.response.RoleSimpleResponse;
import com.proyecto.fhce.library.dto.response.UsuarioResponse;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.security.jwt.JwtTokenProvider;
import com.proyecto.fhce.library.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
public class AuthController {
  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserService usuarioService;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
              request.getUsername(),
              request.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      String token = tokenProvider.generateToken(authentication);

      // UsuarioResponse usuario =
      // usuarioService.findByUsername(request.getUsername());
      Usuario usuario = usuarioService.findByUsername(request.getUsername())
          .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

      LoginResponse response = new LoginResponse();
      response.setToken(token);
      response.setId(usuario.getId_usuario());
      response.setUsername(usuario.getUsername());
      response.setEmail(usuario.getPersona().getEmail());
      response.setRoles(usuario.getRoles().stream()
          .map(role -> role.getName())
          .collect(Collectors.toSet()));
      // response.setPersona(usuario.getPersona());
      // Mapeo Persona a PersonaResponse
      Persona persona = usuario.getPersona();
      PersonaResponse personaResponse = new PersonaResponse();
      personaResponse.setId_persona(persona.getId_persona());
      personaResponse.setNombre(persona.getNombre());
      personaResponse.setEmail(persona.getEmail());
      response.setPersona(personaResponse);

      // usuarioService.resetFailedAttempts(usuario.getId_usuario());

      return ResponseEntity.ok(ApiResponse.success("Login exitoso", response));

    } catch (BadCredentialsException e) {
      // usuarioService.incrementFailedAttempts(request.getUsername());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.error("Credenciales inválidas"));
    }
  }

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UsuarioResponse>> register(@Valid @RequestBody RegisterRequest request) {
    UsuarioResponse usuario = usuarioService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Usuario registrado exitosamente", usuario));
  }

  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> logout() {
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(ApiResponse.success("Logout exitoso", null));
  }

  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Optional<Usuario>>> getCurrentUser(Authentication authentication) {
    String username = authentication.getName();
    Optional<Usuario> usuario = usuarioService.findByUsername(username);
    return ResponseEntity.ok(ApiResponse.success(usuario));
  }
}
