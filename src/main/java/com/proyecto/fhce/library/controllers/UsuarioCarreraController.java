package com.proyecto.fhce.library.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.users.UsuarioCarreraRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.CarreraSimpleResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioCarreraResponse;
import com.proyecto.fhce.library.services.UsuarioCarreraService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios-carreras")
@Tag(name = "Usuario-Carrera", description = "Gestión de asignación de usuarios a carreras")
@SecurityRequirement(name = "bearer-jwt")
public class UsuarioCarreraController {

  @Autowired
  private UsuarioCarreraService usuarioCarreraService;

  @Operation(summary = "Obtener carreras de un usuario", description = "Lista todas las carreras asignadas a un usuario")
  @GetMapping("/usuario/{usuarioId}")
  @PreAuthorize("hasAuthority('USUARIOS_VER') or #usuarioId == authentication.principal.id")
  public ResponseEntity<ApiResponse<List<CarreraSimpleResponse>>> findCarrerasByUsuario(
      @PathVariable Long usuarioId) {
    List<CarreraSimpleResponse> carreras = usuarioCarreraService.findCarrerasByUsuario(usuarioId);
    return ResponseEntity.ok(ApiResponse.success(carreras));
  }

  @Operation(summary = "Obtener usuarios de una carrera", description = "Lista todos los usuarios asignados a una carrera")
  @GetMapping("/carrera/{carreraId}")
  @PreAuthorize("hasAuthority('CARRERAS_VER') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<UsuarioCarreraResponse>>> findUsuariosByCarrera(
      @PathVariable Long carreraId) {
    List<UsuarioCarreraResponse> usuarios = usuarioCarreraService.findUsuariosByCarrera(carreraId);
    return ResponseEntity.ok(ApiResponse.success(usuarios));
  }

  @Operation(summary = "Asignar carrera a usuario", description = "Crea una nueva asignación de carrera para un usuario")
  @PostMapping
  @PreAuthorize("hasAuthority('USUARIOS_EDITAR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UsuarioCarreraResponse>> asignarCarrera(
      @Valid @RequestBody UsuarioCarreraRequest request) {
    UsuarioCarreraResponse asignacion = usuarioCarreraService.asignarCarrera(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Carrera asignada exitosamente", asignacion));
  }

  @Operation(summary = "Actualizar asignación", description = "Actualiza información de la asignación (matrícula, año ingreso, estado)")
  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('USUARIOS_EDITAR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UsuarioCarreraResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody UsuarioCarreraRequest request) {
    UsuarioCarreraResponse asignacion = usuarioCarreraService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Asignación actualizada exitosamente", asignacion));
  }

  @Operation(summary = "Remover carrera de usuario", description = "Elimina la asignación de una carrera a un usuario")
  @DeleteMapping("/usuario/{usuarioId}/carrera/{carreraId}")
  @PreAuthorize("hasAuthority('USUARIOS_EDITAR') or hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> removerCarrera(
      @PathVariable Long usuarioId,
      @PathVariable Long carreraId) {
    usuarioCarreraService.removerCarrera(usuarioId, carreraId);
    return ResponseEntity.ok(ApiResponse.success("Carrera removida exitosamente", null));
  }
}