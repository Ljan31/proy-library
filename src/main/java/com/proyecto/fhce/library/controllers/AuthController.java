package com.proyecto.fhce.library.controllers;

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
import com.proyecto.fhce.library.dto.response.UsuarioResponse;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.security.jwt.JwtTokenProvider;
import com.proyecto.fhce.library.services.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
// @CrossOrigin(origins = "*")
@Tag(name = "Autenticación", description = "Endpoints para autenticación y registro de usuarios")
public class AuthController {
  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserService usuarioService;

  @Autowired
  private JwtTokenProvider tokenProvider;

  @Operation(summary = "Iniciar sesión", description = "Autentica un usuario y retorna un token JWT", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"success\": false, \"message\": \"Credenciales inválidas\"}")))
  })
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
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<UsuarioResponse>> register(@Valid @RequestBody RegisterRequest request) {
    UsuarioResponse usuario = usuarioService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Usuario registrado exitosamente", usuario));
  }

  @Operation(summary = "Cerrar sesión", description = "Cierra la sesión del usuario actual", security = @SecurityRequirement(name = "bearer-jwt"))
  @PostMapping("/logout")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<Void>> logout() {
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(ApiResponse.success("Logout exitoso", null));
  }

  @Operation(summary = "Obtener usuario actual", description = "Retorna la información del usuario autenticado", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetMapping("/me")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<UsuarioResponse>> getCurrentUser(Authentication authentication) {
    String username = authentication.getName();
    // Optional<Usuario> usuario = usuarioService.findByUsername(username);

    Usuario usuario = usuarioService.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    // return ResponseEntity.ok(ApiResponse.success(usuario));
    UsuarioResponse response = new UsuarioResponse();
    response.setId_usuario(usuario.getId_usuario());
    response.setUsername(usuario.getUsername());
    response.setEnabled(usuario.isEnabled());

    // Persona
    PersonaResponse persona = new PersonaResponse();
    persona.setNombre(usuario.getPersona().getNombre());
    persona.setApellido_pat(usuario.getPersona().getApellido_pat());
    persona.setApellido_mat(usuario.getPersona().getApellido_mat());
    persona.setEmail(usuario.getPersona().getEmail());
    response.setPersona(persona);

    // Roles
    // Set<RoleSimpleResponse> roles = usuario.getRoles().stream()
    // .map(role -> new RoleSimpleResponse(
    // role.getId_role(),
    // role.getName()))
    // .collect(Collectors.toSet());

    return ResponseEntity.ok(
        ApiResponse.success("Usuario autenticado", response));
  }
}
