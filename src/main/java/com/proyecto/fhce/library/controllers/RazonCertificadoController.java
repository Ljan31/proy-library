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

import com.proyecto.fhce.library.dto.request.library.RazonCertificadoRequestDTO;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.library.RazonCertificadoResponseDTO;
import com.proyecto.fhce.library.services.library.RazonCertificadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/razones-certificado")
@Tag(name = "Razones de Certificado", description = "Gestión de razones de certificados por biblioteca")
public class RazonCertificadoController {

  @Autowired
  private RazonCertificadoService razonService;

  // =========================
  // LISTAR TODAS
  // =========================
  @Operation(summary = "Listar todas las razones", description = "Obtiene todas las razones de certificados registradas")
  @GetMapping
  public ResponseEntity<ApiResponse<List<RazonCertificadoResponseDTO>>> findAll() {
    List<RazonCertificadoResponseDTO> data = razonService.listarTodas();
    return ResponseEntity.ok(ApiResponse.success(data));
  }

  // =========================
  // POR BIBLIOTECA
  // =========================
  @Operation(summary = "Listar razones por biblioteca", description = "Obtiene las razones de certificado de una biblioteca específica")
  @GetMapping("/biblioteca/{bibliotecaId}")
  public ResponseEntity<ApiResponse<List<RazonCertificadoResponseDTO>>> findByBiblioteca(
      @PathVariable Long bibliotecaId) {

    List<RazonCertificadoResponseDTO> data = razonService.listarPorBiblioteca(bibliotecaId);

    return ResponseEntity.ok(ApiResponse.success(data));
  }

  // =========================
  // OBTENER POR ID
  // =========================
  @Operation(summary = "Obtener razón por ID", description = "Retorna una razón de certificado específica")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<RazonCertificadoResponseDTO>> findById(
      @PathVariable Long id) {

    RazonCertificadoResponseDTO data = razonService.obtenerPorId(id);
    return ResponseEntity.ok(ApiResponse.success(data));
  }

  // =========================
  // CREAR
  // =========================
  @Operation(summary = "Crear razón de certificado", description = "Registra una nueva razón de certificado en una biblioteca")
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<RazonCertificadoResponseDTO>> create(
      @Valid @RequestBody RazonCertificadoRequestDTO request) {

    RazonCertificadoResponseDTO data = razonService.crear(request);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Razón creada exitosamente", data));
  }

  // =========================
  // ACTUALIZAR
  // =========================
  @Operation(summary = "Actualizar razón", description = "Actualiza una razón de certificado existente")
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<RazonCertificadoResponseDTO>> update(
      @PathVariable Long id,
      @Valid @RequestBody RazonCertificadoRequestDTO request) {

    RazonCertificadoResponseDTO data = razonService.actualizar(id, request);

    return ResponseEntity.ok(
        ApiResponse.success("Razón actualizada exitosamente", data));
  }

  // =========================
  // ELIMINAR
  // =========================
  @Operation(summary = "Eliminar razón", description = "Elimina una razón de certificado")
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {

    razonService.eliminar(id);

    return ResponseEntity.ok(
        ApiResponse.success("Razón eliminada exitosamente", null));
  }
}