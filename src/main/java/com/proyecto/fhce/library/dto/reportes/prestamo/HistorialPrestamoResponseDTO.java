package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.util.List;

public class HistorialPrestamoResponseDTO {
  private ResumenHistorialDTO resumen;
  private List<HistorialPrestamoDTO> prestamos;

  public ResumenHistorialDTO getResumen() {
    return resumen;
  }

  public void setResumen(ResumenHistorialDTO resumen) {
    this.resumen = resumen;
  }

  public List<HistorialPrestamoDTO> getPrestamos() {
    return prestamos;
  }

  public void setPrestamos(List<HistorialPrestamoDTO> prestamos) {
    this.prestamos = prestamos;
  }

}