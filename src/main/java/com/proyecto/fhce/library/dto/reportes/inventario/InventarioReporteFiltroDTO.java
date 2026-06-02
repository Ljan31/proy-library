package com.proyecto.fhce.library.dto.reportes.inventario;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

public class InventarioReporteFiltroDTO {
  private Long bibliotecaId;
  private Long categoriaId;
  private EstadoEjemplar estado;
  private String clasificacionDecimal;

  public InventarioReporteFiltroDTO() {
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public Long getCategoriaId() {
    return categoriaId;
  }

  public void setCategoriaId(Long categoriaId) {
    this.categoriaId = categoriaId;
  }

  public EstadoEjemplar getEstado() {
    return estado;
  }

  public void setEstado(EstadoEjemplar estado) {
    this.estado = estado;
  }

  public String getClasificacionDecimal() {
    return clasificacionDecimal;
  }

  public void setClasificacionDecimal(String clasificacionDecimal) {
    this.clasificacionDecimal = clasificacionDecimal;
  }
}
