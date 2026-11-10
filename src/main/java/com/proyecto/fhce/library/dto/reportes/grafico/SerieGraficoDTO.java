package com.proyecto.fhce.library.dto.reportes.grafico;

import java.util.List;

/**
 * Una serie de datos. {@code valores.get(i)} corresponde a
 * {@code GraficoDTO.etiquetas.get(i)}; ambas listas tienen siempre el mismo
 * tamaño.
 */
public record SerieGraficoDTO(String nombre, List<Number> valores) {
}