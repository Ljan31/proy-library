package com.proyecto.fhce.library.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.PersonaRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.PersonaResponse;
import com.proyecto.fhce.library.services.users.PersonaService;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/personas")
@Tag(name = "Personas", description = "Endpoints para personas")
public class PersonaController {

  @Autowired
  private PersonaService personaService;

  // ===================== CREATE =====================
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<ApiResponse<PersonaResponse>> create(
      @RequestBody PersonaRequest request) {

    PersonaResponse response = personaService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Persona creada correctamente", response));
  }

  // ===================== UPDATE =====================
  @PutMapping("/{id}")
  // @PreAuthorize("hasAnyRole('ADMIN')")
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO') or @securityService.isOwner(#id, authentication)")
  public ResponseEntity<ApiResponse<PersonaResponse>> update(
      @PathVariable Long id,
      @RequestBody PersonaRequest request) {

    PersonaResponse response = personaService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Persona actualizada correctamente", response));
  }

  // ===================== FIND BY ID =====================
  @GetMapping("/{id}")
  // @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO') or #id ==
  // principal.getId_persona")
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO') or @securityService.isOwner(#id, authentication)")
  public ResponseEntity<ApiResponse<PersonaResponse>> findById(
      @PathVariable Long id) {

    PersonaResponse response = personaService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(response));
  }

  // @GetMapping("/{id}")
  // @PreAuthorize("isAuthenticated()")
  // public ResponseEntity<ApiResponse<PersonaResponse>> findById(
  // @PathVariable Long id,
  // Authentication authentication) {

  // PersonaResponse response = personaService.findByIdSecure(id,
  // authentication.getName());
  // return ResponseEntity.ok(ApiResponse.success(response));
  // }
}