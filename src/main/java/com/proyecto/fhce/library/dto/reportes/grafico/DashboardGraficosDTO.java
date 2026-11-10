package com.proyecto.fhce.library.dto.reportes.grafico;

/**
 * Los gráficos más importantes para el bibliotecario, en UNA sola petición.
 * Ventana de tiempo de los gráficos de préstamos: últimos 6 meses (mes actual
 * incluido). vencidosPorTramo y estadoEjemplares son una foto de HOY.
 */
public record DashboardGraficosDTO(
    GraficoDTO prestamosPorMes,
    GraficoDTO librosMasPrestados,
    GraficoDTO prestamosPorEstado,
    GraficoDTO vencidosPorTramo,
    GraficoDTO estadoEjemplares) {
}