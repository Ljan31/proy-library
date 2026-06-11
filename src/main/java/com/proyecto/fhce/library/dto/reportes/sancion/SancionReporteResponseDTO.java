package com.proyecto.fhce.library.dto.reportes.sancion;

import java.util.List;

public class SancionReporteResponseDTO {
  private SancionResumenDTO resumen;
  private List<SancionReporteItemDTO> sanciones;

  public SancionResumenDTO getResumen() {
    return resumen;
  }

  public void setResumen(SancionResumenDTO resumen) {
    this.resumen = resumen;
  }

  public List<SancionReporteItemDTO> getSanciones() {
    return sanciones;
  }

  public void setSanciones(List<SancionReporteItemDTO> sanciones) {
    this.sanciones = sanciones;
  }

}