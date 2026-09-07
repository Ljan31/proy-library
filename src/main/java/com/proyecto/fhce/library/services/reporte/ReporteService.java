package com.proyecto.fhce.library.services.reporte;

import java.time.LocalDate;
import java.util.List;

import com.proyecto.fhce.library.dto.reportes.DashboardDTO;
import com.proyecto.fhce.library.dto.reportes.InventarioBibliotecaDTO;
import com.proyecto.fhce.library.dto.reportes.ReportePrestamoDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.DeterioroReporteResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EjemplarNuncaPrestadoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.EstadoEjemplarBibliotecaResponseDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioReporteDTO;
import com.proyecto.fhce.library.dto.reportes.inventario.InventarioReporteFiltroDTO;

public interface ReporteService {
  DashboardDTO obtenerDashboard(Long bibliotecaId);

  List<ReportePrestamoDTO> reportePrestamos(
      Long bibliotecaId,
      LocalDate fechaInicio,
      LocalDate fechaFin);

  InventarioBibliotecaDTO inventarioBiblioteca(
      Long bibliotecaId);

  InventarioReporteDTO generarInventario(
      InventarioReporteFiltroDTO filtro);

  EstadoEjemplarBibliotecaResponseDTO obtenerEstadoEjemplaresPorBiblioteca(
      Long bibliotecaId);

  DeterioroReporteResponseDTO reporteDeterioro(
      Long bibliotecaId, LocalDate fechaInicio, LocalDate fechaFin);

  EjemplarNuncaPrestadoResponseDTO reporteEjemplaresNuncaPrestados(
      Long bibliotecaId);
}
