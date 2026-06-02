package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.util.List;

public class PrestamoReporteResponseDTO {
  private ResumenPrestamoDTO resumen;

  private List<PrestamoReporteDTO> prestamos;

  public PrestamoReporteResponseDTO() {
  }

  public ResumenPrestamoDTO getResumen() {
    return resumen;
  }

  public void setResumen(ResumenPrestamoDTO resumen) {
    this.resumen = resumen;
  }

  public List<PrestamoReporteDTO> getPrestamos() {
    return prestamos;
  }

  public void setPrestamos(List<PrestamoReporteDTO> prestamos) {
    this.prestamos = prestamos;
  }

}
