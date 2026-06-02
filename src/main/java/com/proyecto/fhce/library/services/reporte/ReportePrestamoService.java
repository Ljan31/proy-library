package com.proyecto.fhce.library.services.reporte;

import com.proyecto.fhce.library.dto.reportes.prestamo.HistorialPrestamoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.LibroMasPrestadoResponseDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoFiltroDTO;
import com.proyecto.fhce.library.dto.reportes.prestamo.PrestamoReporteResponseDTO;

public interface ReportePrestamoService {
  PrestamoReporteResponseDTO generarReportePrestamos(
      PrestamoFiltroDTO filtro);

  HistorialPrestamoDTO historialPrestamos(
      HistorialPrestamoDTO filtro);

  LibroMasPrestadoResponseDTO obtenerLibrosMasPrestados(
      Long bibliotecaId);

  LibroMasPrestadoDTO librosMenosPrestados(
      LibroMasPrestadoDTO filtro);
}
