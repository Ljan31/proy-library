package com.proyecto.fhce.library.dto.reportes.inventario;

public class ResumenEstadoEjemplarDTO {
  private Long totalBibliotecas;

  private Long totalEjemplares;

  public ResumenEstadoEjemplarDTO() {
  }

  public Long getTotalBibliotecas() {
    return totalBibliotecas;
  }

  public void setTotalBibliotecas(Long totalBibliotecas) {
    this.totalBibliotecas = totalBibliotecas;
  }

  public Long getTotalEjemplares() {
    return totalEjemplares;
  }

  public void setTotalEjemplares(Long totalEjemplares) {
    this.totalEjemplares = totalEjemplares;
  }

}
