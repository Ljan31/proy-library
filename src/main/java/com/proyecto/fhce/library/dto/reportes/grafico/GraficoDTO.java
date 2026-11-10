package com.proyecto.fhce.library.dto.reportes.grafico;

import java.util.List;

/**
 * Contrato único para TODOS los gráficos: etiquetas + series.
 * El frontend usa un solo componente que lo dibuja según {@code tipoSugerido}.
 *
 * @param id           identificador estable (p. ej. "prestamos-por-mes"), útil
 *                     para claves de v-for y para nombrar la imagen en el PDF
 * @param titulo       título listo para mostrar
 * @param tipoSugerido ver {@link TipoGrafico}
 * @param unidad       qué cuentan los valores ("préstamos", "ejemplares", "Bs")
 * @param etiquetas    eje X (o porciones, si es DONA)
 * @param series       una o más series; DONA siempre trae exactamente una
 * @param total        suma de todos los valores
 * @param sinDatos     true si no hay nada que dibujar (mostrar estado vacío)
 */
public record GraficoDTO(
    String id,
    String titulo,
    TipoGrafico tipoSugerido,
    String unidad,
    List<String> etiquetas,
    List<SerieGraficoDTO> series,
    double total,
    boolean sinDatos) {

  public static GraficoDTO of(
      String id, String titulo, TipoGrafico tipo, String unidad,
      List<String> etiquetas, List<SerieGraficoDTO> series) {
    double total = series.stream()
        .flatMap(s -> s.valores().stream())
        .mapToDouble(Number::doubleValue)
        .sum();
    return new GraficoDTO(id, titulo, tipo, unidad, etiquetas, series, total, total == 0);
  }
}