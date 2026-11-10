package com.proyecto.fhce.library.dto.reportes.grafico;

/**
 * Tipo de gráfico que el backend SUGIERE al frontend para esa serie de datos.
 * El frontend debe respetarlo: está elegido según la naturaleza del dato
 * (tendencia, ranking, composición).
 */
public enum TipoGrafico {
  /** Tendencia en el tiempo (etiquetas = períodos ordenados). */
  LINEA,
  /** Comparación entre pocas categorías. */
  BARRAS,
  /** Ranking con etiquetas largas (títulos de libros, áreas). */
  BARRAS_HORIZONTALES,
  /** Composición por categoría y subcategoría (una serie por subcategoría). */
  BARRAS_APILADAS,
  /** Composición de un total en pocas partes (una sola serie). */
  DONA
}