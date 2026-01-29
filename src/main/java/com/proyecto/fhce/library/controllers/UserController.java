package com.proyecto.fhce.library.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.ChangePasswordRequest;
import com.proyecto.fhce.library.dto.request.RegisterRequest;
import com.proyecto.fhce.library.dto.request.UsuarioUpdateRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.UsuarioResponse;
import com.proyecto.fhce.library.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {
  @Autowired
  private UserService userService;

  // ===================== CREATE =====================
  @PostMapping
  public ResponseEntity<ApiResponse<UsuarioResponse>> create(
      @RequestBody RegisterRequest request) {

    UsuarioResponse response = userService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Usuario creado correctamente", response));
  }

  // ===================== UPDATE =====================
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UsuarioResponse>> update(
      @PathVariable Long id,
      @RequestBody UsuarioUpdateRequest request) {

    UsuarioResponse response = userService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Usuario actualizado", response));
  }

  // ===================== CHANGE PASSWORD =====================
  @PutMapping("/{id}/change-password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @PathVariable Long id,
      @RequestBody ChangePasswordRequest request) {

    userService.changePassword(id, request);
    return ResponseEntity.ok(ApiResponse.success("Password actualizado", null));
  }

  // ===================== ENABLE / DISABLE =====================
  @PutMapping("/{id}/toggle-enabled")
  public ResponseEntity<ApiResponse<Void>> toggleEnabled(@PathVariable Long id) {

    userService.toggleEnabled(id);
    return ResponseEntity.ok(ApiResponse.success("Estado del usuario actualizado", null));
  }

  // ===================== FIND BY ID =====================
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UsuarioResponse>> findById(@PathVariable Long id) {

    UsuarioResponse response = userService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // ===================== FIND ALL =====================
  @GetMapping
  public ResponseEntity<ApiResponse<List<UsuarioResponse>>> findAll() {

    List<UsuarioResponse> response = userService.findAll();
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // ===================== FIND BY ROLE =====================
  @GetMapping("/by-role")
  public ResponseEntity<ApiResponse<List<UsuarioResponse>>> findByRole(
      @RequestParam String roleName) {

    List<UsuarioResponse> response = userService.findByRoleName(roleName);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // ===================== SEARCH =====================
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<UsuarioResponse>>> search(
      @RequestParam String q) {

    List<UsuarioResponse> response = userService.search(q);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}
