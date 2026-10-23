package com.proyecto.fhce.library.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.AuditoriaResponse;
import com.proyecto.fhce.library.services.AuditoriaService;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

  private final AuditoriaService auditoriaService;

  public AuditoriaController(AuditoriaService auditoriaService) {
    this.auditoriaService = auditoriaService;
  }

  // Obtener todos los registros de auditoría
  @GetMapping
  public ResponseEntity<ApiResponse<List<AuditoriaResponse>>> findAll() {
    List<AuditoriaResponse> auditorias = auditoriaService.findAll();

    return ResponseEntity.ok(
        ApiResponse.success(auditorias));
  }

  // Consultar auditorías de un usuario
  @GetMapping("/usuario/{usuarioId}")
  public ResponseEntity<ApiResponse<List<AuditoriaResponse>>> findByUsuario(
      @PathVariable Long usuarioId) {

    List<AuditoriaResponse> auditorias = auditoriaService.findByUsuario(usuarioId);

    return ResponseEntity.ok(
        ApiResponse.success(auditorias));
  }

  // Consultar auditorías de una tabla
  @GetMapping("/tabla/{tabla}")
  public ResponseEntity<ApiResponse<List<AuditoriaResponse>>> findByTabla(
      @PathVariable String tabla) {

    List<AuditoriaResponse> auditorias = auditoriaService.findByTabla(tabla);

    return ResponseEntity.ok(
        ApiResponse.success(auditorias));
  }

  // Consultar historial de un registro específico
  @GetMapping("/registro/{tabla}/{idRegistro}")
  public ResponseEntity<ApiResponse<List<AuditoriaResponse>>> findByRegistro(
      @PathVariable String tabla,
      @PathVariable Long idRegistro) {

    List<AuditoriaResponse> auditorias = auditoriaService.findByRegistro(tabla, idRegistro);

    return ResponseEntity.ok(
        ApiResponse.success(auditorias));
  }

  // Consultar auditorías por acción: CREAR, ACTUALIZAR, ELIMINAR
  @GetMapping("/accion/{accion}")
  public ResponseEntity<ApiResponse<List<AuditoriaResponse>>> findByAccion(
      @PathVariable String accion) {

    List<AuditoriaResponse> auditorias = auditoriaService.findByAccion(accion);

    return ResponseEntity.ok(
        ApiResponse.success(auditorias));
  }

  // Consultar auditorías por rango de fechas
  @GetMapping("/fechas")
  public ResponseEntity<ApiResponse<List<AuditoriaResponse>>> findByFechas(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,

      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

    if (inicio.isAfter(fin)) {
      return ResponseEntity.badRequest().body(
          ApiResponse.error("La fecha inicial no puede ser posterior a la fecha final"));
    }

    List<AuditoriaResponse> auditorias = auditoriaService.findByFechas(inicio, fin);

    return ResponseEntity.ok(
        ApiResponse.success(auditorias));
  }
}