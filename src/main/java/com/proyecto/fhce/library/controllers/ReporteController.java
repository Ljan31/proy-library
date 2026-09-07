package com.proyecto.fhce.library.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.reportes.DashboardDTO;
import com.proyecto.fhce.library.dto.reportes.certificado.CertificadoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.certificado.CertificadoReporteResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.DeterioroReporteResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EjemplarNuncaPrestadoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EstadoEjemplarBibliotecaResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioReporteDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioReporteFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.HistorialPrestamoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.HistorialPrestamoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoReporteResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamosActivosResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.ResumenLibrosPrestadosDTO;
import com.proyecto.fhce.library.dto.reportes.sancion.SancionFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.sancion.SancionReporteResponseDTO;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.services.reporte.ReporteCertificadoService;
import com.proyecto.fhce.library.services.reporte.ReportePrestamoService;
import com.proyecto.fhce.library.services.reporte.ReporteSancionService;
import com.proyecto.fhce.library.services.reporte.ReporteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes")
public class ReporteController {

  @Autowired
  private ReporteService reporteService;

  @Autowired
  private ReportePrestamoService reportePrestamoService;
  @Autowired
  private ReporteSancionService reporteSancionService;
  @Autowired
  private ReporteCertificadoService reporteCertificadoService;

  @GetMapping("/dashboard")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN', 'ESTUDIANTE')")
  public ResponseEntity<ApiResponse<DashboardDTO>> dashboard(
      @RequestParam(required = false) Long bibliotecaId) {

    DashboardDTO dto = reporteService.obtenerDashboard(bibliotecaId);

    return ResponseEntity.ok(
        ApiResponse.success(dto));
  }

  @Operation(summary = "Reporte de inventario")
  @GetMapping("/inventario")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<InventarioReporteDTO>> generarInventario(

      @RequestParam(required = false) Long bibliotecaId,

      @RequestParam(required = false) Long categoriaId,

      @RequestParam(required = false) EstadoEjemplar estado,

      @RequestParam(required = false) String clasificacionDecimal) {

    InventarioReporteFiltroDTO filtro = new InventarioReporteFiltroDTO();

    filtro.setBibliotecaId(bibliotecaId);

    filtro.setCategoriaId(categoriaId);

    filtro.setEstado(estado);

    filtro.setClasificacionDecimal(clasificacionDecimal);

    InventarioReporteDTO reporte = reporteService.generarInventario(filtro);

    return ResponseEntity.ok(ApiResponse.success(reporte));
  }

  @PostMapping("/prestamos")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<PrestamoReporteResponseDTO>> reportePrestamos(
      @RequestBody PrestamoFiltroDTO filtro) {

    PrestamoReporteResponseDTO reporte = reportePrestamoService.generarReportePrestamos(filtro);

    return ResponseEntity.ok(
        ApiResponse.success(reporte));
  }

  @Operation(summary = "Reporte de estado de ejemplares por biblioteca")
  @GetMapping("/estado-ejemplares")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<EstadoEjemplarBibliotecaResponseDTO>> estadoEjemplares(

      @RequestParam(required = false) Long bibliotecaId) {

    EstadoEjemplarBibliotecaResponseDTO reporte = reporteService
        .obtenerEstadoEjemplaresPorBiblioteca(
            bibliotecaId);

    return ResponseEntity.ok(
        ApiResponse.success(reporte));
  }

  @Operation(summary = "Reporte de libros más prestados")
  @GetMapping("/libros-mas-prestados")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<LibroMasPrestadoResponseDTO>> obtenerLibrosMasPrestados(

      @RequestParam(required = false) Long bibliotecaId) {

    LibroMasPrestadoResponseDTO reporte = reportePrestamoService
        .obtenerLibrosMasPrestados(
            bibliotecaId);

    return ResponseEntity.ok(
        ApiResponse.success(reporte));
  }

  @Operation(summary = "Historial de préstamos con filtros avanzados")
  @PostMapping("/prestamos/historial")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<HistorialPrestamoResponseDTO>> historialPrestamos(
      @RequestBody HistorialPrestamoFiltroDTO filtro) {
    return ResponseEntity.ok(ApiResponse.success(
        reportePrestamoService.historialPrestamos(filtro)));
  }

  @Operation(summary = "Libros menos prestados por biblioteca")
  @GetMapping("/libros-menos-prestados")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<LibroMasPrestadoResponseDTO>> obtenerLibrosMenosPrestados(
      @RequestParam(required = false) Long bibliotecaId) {
    // Reutiliza el mismo DTO invertido — lista libros en orden ascendente

    LibroMasPrestadoResponseDTO reporte = reportePrestamoService
        .librosMenosPrestados(
            bibliotecaId);

    return ResponseEntity.ok(
        ApiResponse.success(reporte));
  }

  @Operation(summary = "Reporte de deterioro de ejemplares")
  @GetMapping("/deterioro-ejemplares")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<DeterioroReporteResponseDTO>> reporteDeterioro(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
    return ResponseEntity.ok(ApiResponse.success(
        reporteService.reporteDeterioro(bibliotecaId, fechaInicio, fechaFin)));
  }

  @Operation(summary = "Ejemplares que nunca han sido prestados")
  @GetMapping("/ejemplares-sin-circulacion")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<EjemplarNuncaPrestadoResponseDTO>> ejemplaresSinCirculacion(
      @RequestParam(required = false) Long bibliotecaId) {
    return ResponseEntity.ok(ApiResponse.success(
        reporteService.reporteEjemplaresNuncaPrestados(bibliotecaId)));
  }

  @Operation(summary = "Reporte de sanciones con filtros")
  @PostMapping("/sanciones")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<SancionReporteResponseDTO>> reporteSanciones(
      @RequestBody SancionFiltroDTO filtro) {
    return ResponseEntity.ok(ApiResponse.success(
        reporteSancionService.generarReporteSanciones(filtro)));
  }

  @Operation(summary = "Reporte de certificados de no deuda")
  @PostMapping("/certificados")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<CertificadoReporteResponseDTO>> reporteCertificados(
      @RequestBody CertificadoFiltroDTO filtro) {
    return ResponseEntity.ok(ApiResponse.success(
        reporteCertificadoService.generarReporteCertificados(filtro)));
  }

  /**
   * Reporte 1 — Préstamos activos del día.
   * El ESTUDIANTE puede verlo para sus propios préstamos (filtrará por usuarioId
   * en el front).
   * BIBLIOTECARIO y ADMIN ven todos los de su biblioteca.
   */
  @Operation(summary = "Préstamos activos del día")
  @GetMapping("/prestamos-activos")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<PrestamosActivosResponseDTO>> prestamosActivosDelDia(
      @RequestParam(required = false) Long bibliotecaId) {

    return ResponseEntity.ok(ApiResponse.success(
        reportePrestamoService.reportePrestamosActivos(bibliotecaId)));
  }

  /**
   * Reporte 2 — Devoluciones pendientes vencidas.
   * El bibliotecario abre esto para ver qué libros no fueron devueltos a tiempo.
   */
  @Operation(summary = "Devoluciones pendientes vencidas")
  @GetMapping("/devoluciones-pendientes")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<PrestamosActivosResponseDTO>> devolucionesPendientesVencidas(
      @RequestParam(required = false) Long bibliotecaId) {

    return ResponseEntity.ok(ApiResponse.success(
        reportePrestamoService.reporteDevolucionesPendientes(bibliotecaId)));
  }

  /**
   * Reporte 3 — Préstamos por vencer en N días.
   * Default: 3 días. El bibliotecario puede ajustar la ventana.
   */
  @Operation(summary = "Préstamos próximos a vencer")
  @GetMapping("/prestamos-por-vencer")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<PrestamosActivosResponseDTO>> prestamosPorVencer(
      @RequestParam(required = false) Long bibliotecaId,
      @RequestParam(defaultValue = "3") int dias) {

    if (dias < 1 || dias > 30) {
      throw new BusinessException("El parámetro 'dias' debe estar entre 1 y 30");
    }

    return ResponseEntity.ok(ApiResponse.success(
        reportePrestamoService.reportePrestamosPorVencer(bibliotecaId, dias)));
  }
}