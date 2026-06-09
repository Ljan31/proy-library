package com.proyecto.fhce.library.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.reportes.DashboardDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EstadoEjemplarBibliotecaResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioReporteDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioReporteFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoReporteResponseDTO;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.services.reporte.ReportePrestamoService;
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
}