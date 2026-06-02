package com.proyecto.fhce.library.dto.reportes.inventario;

import java.util.List;

public class InventarioReporteDTO {
  private InventarioResumenDTO resumen;

  private List<InventarioItemDTO> detalle;

  public InventarioReporteDTO() {
  }

  public InventarioResumenDTO getResumen() {
    return resumen;
  }

  public void setResumen(InventarioResumenDTO resumen) {
    this.resumen = resumen;
  }

  public List<InventarioItemDTO> getDetalle() {
    return detalle;
  }

  public void setDetalle(List<InventarioItemDTO> detalle) {
    this.detalle = detalle;
  }
}
