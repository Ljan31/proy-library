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

import com.proyecto.fhce.library.dto.request.CarreraRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.CarreraDetalleResponse;
import com.proyecto.fhce.library.dto.response.CarreraResponse;
import com.proyecto.fhce.library.services.CarreraService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/carreras")
@Tag(name = "Carreras", description = "Gestión de carreras académicas")
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

  @GetMapping("/{id}/detalle")
  public ResponseEntity<ApiResponse<CarreraDetalleResponse>> findDetalle(@PathVariable Long id) {
    CarreraDetalleResponse detalle = carreraService.findByIdWithDetalle(id);
    return ResponseEntity.ok(ApiResponse.success(detalle));
  }

  @GetMapping("/codigo/{codigo}")
  public ResponseEntity<ApiResponse<CarreraResponse>> findByCodigo(@PathVariable String codigo) {
    CarreraResponse carrera = carreraService.findByCodigo(codigo);
    return ResponseEntity.ok(ApiResponse.success(carrera));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<CarreraResponse>>> search(
      @RequestParam String nombre) {

    List<CarreraResponse> carreras = carreraService.search(nombre);
    return ResponseEntity.ok(ApiResponse.success(carreras));
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

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    carreraService.delete(id);
    return ResponseEntity.ok(
        ApiResponse.success("Carrera eliminada exitosamente", null));
  }
}
