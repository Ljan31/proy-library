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
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.users.RoleRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.users.RoleResponse;
import com.proyecto.fhce.library.services.users.RoleService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "Endpoints para Roles")
public class RoleController {

  @Autowired
  private RoleService roleService;

  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<RoleResponse>>> findAll() {
    List<RoleResponse> roles = roleService.findAll();
    return ResponseEntity.ok(ApiResponse.success(roles));
  }

  @GetMapping("/with-permisos")
  public ResponseEntity<ApiResponse<List<RoleResponse>>> findAllWithPermisos() {
    List<RoleResponse> roles = roleService.findAllWithPermisos();
    return ResponseEntity.ok(ApiResponse.success(roles));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<RoleResponse>> findById(@PathVariable Long id) {
    RoleResponse role = roleService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(role));
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<ApiResponse<RoleResponse>> findByName(@PathVariable String name) {
    RoleResponse role = roleService.findByName(name);
    return ResponseEntity.ok(ApiResponse.success(role));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<RoleResponse>> create(@Valid @RequestBody RoleRequest request) {
    RoleResponse role = roleService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Rol creado exitosamente", role));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<RoleResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody RoleRequest request) {
    RoleResponse role = roleService.update(request, id);
    return ResponseEntity.ok(ApiResponse.success("Rol actualizado exitosamente", role));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    roleService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Rol eliminado exitosamente", null));
  }

  @PostMapping("/{roleId}/permisos/{permisoId}")
  public ResponseEntity<ApiResponse<RoleResponse>> asignarPermiso(
      @PathVariable Long roleId,
      @PathVariable Long permisoId) {
    RoleResponse role = roleService.asignarPermiso(roleId, permisoId);
    return ResponseEntity.ok(ApiResponse.success("Permiso asignado exitosamente", role));
  }

  @DeleteMapping("/{roleId}/permisos/{permisoId}")
  public ResponseEntity<ApiResponse<RoleResponse>> removerPermiso(
      @PathVariable Long roleId,
      @PathVariable Long permisoId) {
    RoleResponse role = roleService.removerPermiso(roleId, permisoId);
    return ResponseEntity.ok(ApiResponse.success("Permiso removido exitosamente", role));
  }
}
