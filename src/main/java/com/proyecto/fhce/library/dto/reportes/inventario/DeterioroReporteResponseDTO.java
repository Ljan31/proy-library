package com.proyecto.fhce.library.dto.reportes.inventario;

import java.util.List;

public class DeterioroReporteResponseDTO {
  private DeterioroResumenDTO resumen;
  private List<DeterioroItemDTO> detalle;

  public DeterioroResumenDTO getResumen() {
    return resumen;
  }

  public void setResumen(DeterioroResumenDTO resumen) {
    this.resumen = resumen;
  }

  public List<DeterioroItemDTO> getDetalle() {
    return detalle;
  }

  public void setDetalle(List<DeterioroItemDTO> detalle) {
    this.detalle = detalle;
  }

}