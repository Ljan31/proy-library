package com.proyecto.fhce.library.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.proyecto.fhce.library.dto.request.CarreraRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.CarreraResponse;
import com.proyecto.fhce.library.services.CarreraService;

import jakarta.validation.Valid;

public class CarreraController {
  @Autowired
  private CarreraService carreraService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CarreraResponse>>> findAll() {
    List<CarreraResponse> carreras = carreraService.findAll();
    return ResponseEntity.ok(ApiResponse.success(carreras));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CarreraResponse>> findById(@PathVariable Long id) {
    CarreraResponse carrera = carreraService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(carrera));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<CarreraResponse>> create(@Valid @RequestBody CarreraRequest request) {
    CarreraResponse carrera = carreraService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Carrera creada exitosamente", carrera));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<CarreraResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody CarreraRequest request) {
    CarreraResponse carrera = carreraService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Carrera actualizada", carrera));
  }
}
