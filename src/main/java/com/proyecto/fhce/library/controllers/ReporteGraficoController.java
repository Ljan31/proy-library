package com.proyecto.fhce.library.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.reportes.grafico.DashboardGraficosDTO;
import com.proyecto.fhce.library.dto.reportes.grafico.GraficoDTO;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.enums.TipoPrestamo;
import com.proyecto.fhce.library.services.reporte.ReporteGraficoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Datos para los gráficos de los reportes y del dashboard. Todos devuelven el
 * mismo contrato {@link GraficoDTO}. Los filtros (biblioteca, fechas) son los
 * mismos que usa el reporte al que acompañan, para que gráfico y tabla cuadren.
 */
@RestController
@RequestMapping("/api/reportes/graficos")
@Tag(name = "Reportes - Gráficos")
@Validated
@PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
public class ReporteGraficoController {

  private final ReporteGraficoService graficoService;

  public ReporteGraficoController(ReporteGraficoService graficoService) {
    this.graficoService = graficoService;
  }

  @Operation(summary = "Gráficos del dashboard del bibliotecario (una sola petición)")
  @GetMapping("/dashboard")
  public ResponseEntity<ApiResponse<DashboardGraficosDTO>> dashboard(
      @RequestParam(required = false) Long bibliotecaId) {
    return ResponseEntity.ok(ApiResponse.success(graficoService.dashboard(bibliotecaId)));
  }

  @Operation(summary = "Línea: préstamos por mes")
  @GetMapping("/prestamos-por-mes")
  public ResponseEntity<ApiResponse<GraficoDTO>> prestamosPorMes(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) EstadoPrestamo estado,
      @RequestParam(required = false) TipoPrestamo tipoPrestamo,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    return ResponseEntity.ok(ApiResponse.success(
        graficoService.prestamosPorMes(bibliotecaId, estado, tipoPrestamo, fechaInicio, fechaFin)));
  }

  @Operation(summary = "Dona: préstamos por estado")
  @GetMapping("/prestamos-por-estado")
  public ResponseEntity<ApiResponse<GraficoDTO>> prestamosPorEstado(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) TipoPrestamo tipoPrestamo,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    return ResponseEntity.ok(ApiResponse.success(
        graficoService.prestamosPorEstado(bibliotecaId, tipoPrestamo, fechaInicio, fechaFin)));
  }

  @Operation(summary = "Dona: préstamos por tipo (sala / domicilio)")
  @GetMapping("/prestamos-por-tipo")
  public ResponseEntity<ApiResponse<GraficoDTO>> prestamosPorTipo(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) EstadoPrestamo estado,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    return ResponseEntity.ok(ApiResponse.success(
        graficoService.prestamosPorTipo(bibliotecaId, estado, fechaInicio, fechaFin)));
  }

  @Operation(summary = "Barras: libros más prestados (top N)")
  @GetMapping("/libros-mas-prestados")
  public ResponseEntity<ApiResponse<GraficoDTO>> librosMasPrestados(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limite) {
    return ResponseEntity.ok(ApiResponse.success(
        graficoService.librosMasPrestados(bibliotecaId, fechaInicio, fechaFin, limite)));
  }

  @Operation(summary = "Barras: libros menos prestados (top N, con al menos 1 préstamo)")
  @GetMapping("/libros-menos-prestados")
  public ResponseEntity<ApiResponse<GraficoDTO>> librosMenosPrestados(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limite) {
    return ResponseEntity.ok(ApiResponse.success(
        graficoService.librosMenosPrestados(bibliotecaId, fechaInicio, fechaFin, limite)));
  }

  @Operation(summary = "Barras: préstamos por área temática (clasificación decimal)")
  @GetMapping("/prestamos-por-area")
  public ResponseEntity<ApiResponse<GraficoDTO>> prestamosPorArea(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    return ResponseEntity.ok(ApiResponse.success(
        graficoService.prestamosPorArea(bibliotecaId, fechaInicio, fechaFin)));
  }

  @Operation(summary = "Barras: devoluciones vencidas por tramo de retraso (foto de hoy)")
  @GetMapping("/vencidos-por-tramo")
  public ResponseEntity<ApiResponse<GraficoDTO>> vencidosPorTramo(
      @RequestParam(required = false) Long bibliotecaId) {
    return ResponseEntity.ok(ApiResponse.success(graficoService.vencidosPorTramo(bibliotecaId)));
  }

  @Operation(summary = "Barras apiladas / dona: estado de ejemplares")
  @GetMapping("/estado-ejemplares")
  public ResponseEntity<ApiResponse<GraficoDTO>> estadoEjemplares(
      @RequestParam(required = false) Long bibliotecaId) {
    return ResponseEntity.ok(ApiResponse.success(graficoService.estadoEjemplares(bibliotecaId)));
  }

  @Operation(summary = "Barras: monto de sanciones por estado")
  @GetMapping("/sanciones-montos")
  public ResponseEntity<ApiResponse<GraficoDTO>> sancionesMontos(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    return ResponseEntity.ok(ApiResponse.success(
        graficoService.sancionesMontos(bibliotecaId, fechaInicio, fechaFin)));
  }

  @Operation(summary = "Dona: sanciones por tipo")
  @GetMapping("/sanciones-por-tipo")
  public ResponseEntity<ApiResponse<GraficoDTO>> sancionesPorTipo(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    return ResponseEntity.ok(ApiResponse.success(
        graficoService.sancionesPorTipo(bibliotecaId, fechaInicio, fechaFin)));
  }
}