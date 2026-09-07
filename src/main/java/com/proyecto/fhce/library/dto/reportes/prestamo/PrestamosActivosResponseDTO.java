package com.proyecto.fhce.library.dto.reportes.prestamo;

import java.util.List;

public class PrestamosActivosResponseDTO {
  private ResumenPrestamosActivosDTO resumen;
  private List<PrestamoActivoDTO> prestamos;

  public ResumenPrestamosActivosDTO getResumen() {
    return resumen;
  }

  public void setResumen(ResumenPrestamosActivosDTO resumen) {
    this.resumen = resumen;
  }

  public List<PrestamoActivoDTO> getPrestamos() {
    return prestamos;
  }

  public void setPrestamos(List<PrestamoActivoDTO> prestamos) {
    this.prestamos = prestamos;
  }

}