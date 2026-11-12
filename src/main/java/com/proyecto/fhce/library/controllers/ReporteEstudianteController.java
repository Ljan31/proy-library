package com.proyecto.fhce.library.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.reportes.estudiante.EstudianteDashboardDTO;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.services.UsuarioActualService;
import com.proyecto.fhce.library.services.reporte.ReporteEstudianteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reportes/dashboard/estudiante")
@Tag(name = "Reportes - Dashboard estudiante")
public class ReporteEstudianteController {

  private final ReporteEstudianteService estudianteService;
  private final UsuarioActualService usuarioActual;

  public ReporteEstudianteController(
      ReporteEstudianteService estudianteService, UsuarioActualService usuarioActual) {
    this.estudianteService = estudianteService;
    this.usuarioActual = usuarioActual;
  }

  /**
   * Sin parámetros: el usuario sale del JWT. Nunca se acepta un id desde el
   * cliente.
   */
  @Operation(summary = "Dashboard del estudiante autenticado")
  @GetMapping
  @PreAuthorize("hasRole('ESTUDIANTE')")
  public ResponseEntity<ApiResponse<EstudianteDashboardDTO>> dashboard() {
    return ResponseEntity.ok(ApiResponse.success(
        estudianteService.obtenerDashboard(usuarioActual.idUsuarioActual())));
  }
}