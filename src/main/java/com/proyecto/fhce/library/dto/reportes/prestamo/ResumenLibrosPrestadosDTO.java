package com.proyecto.fhce.library.dto.reportes.prestamo;

public class ResumenLibrosPrestadosDTO {

  private Long totalLibros;

  private Long totalPrestamos;

  public ResumenLibrosPrestadosDTO() {
  }

  public Long getTotalLibros() {
    return totalLibros;
  }

  public void setTotalLibros(Long totalLibros) {
    this.totalLibros = totalLibros;
  }

  public Long getTotalPrestamos() {
    return totalPrestamos;
  }

  public void setTotalPrestamos(Long totalPrestamos) {
    this.totalPrestamos = totalPrestamos;
  }

}
