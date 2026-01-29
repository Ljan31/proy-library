package com.proyecto.fhce.library.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import com.proyecto.fhce.library.services.PersonaService;

@RestController
@RequestMapping("/api/personas")
public class PersonaController {

  @Autowired
  private PersonaService personaService;

  // ===================== CREATE =====================
  @PostMapping
  public ResponseEntity<ApiResponse<PersonaResponse>> create(
      @RequestBody PersonaRequest request) {

    PersonaResponse response = personaService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Persona creada correctamente", response));
  }

  // ===================== UPDATE =====================
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PersonaResponse>> update(
      @PathVariable Long id,
      @RequestBody PersonaRequest request) {

    PersonaResponse response = personaService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Persona actualizada correctamente", response));
  }

  // ===================== FIND BY ID =====================
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PersonaResponse>> findById(
      @PathVariable Long id) {

    PersonaResponse response = personaService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(response));
  }
}