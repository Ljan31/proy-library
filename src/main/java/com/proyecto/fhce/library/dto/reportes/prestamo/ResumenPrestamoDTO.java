package com.proyecto.fhce.library.dto.reportes.prestamo;

public class ResumenPrestamoDTO {
  private Long totalPrestamos;
  private Long activos;
  private Long vencidos;
  private Long devueltos;
  private Long renovados;

  public ResumenPrestamoDTO() {
  }

  public Long getTotalPrestamos() {
    return totalPrestamos;
  }

  public void setTotalPrestamos(Long totalPrestamos) {
    this.totalPrestamos = totalPrestamos;
  }

  public Long getActivos() {
    return activos;
  }

  public void setActivos(Long activos) {
    this.activos = activos;
  }

  public Long getVencidos() {
    return vencidos;
  }

  public void setVencidos(Long vencidos) {
    this.vencidos = vencidos;
  }

  public Long getDevueltos() {
    return devueltos;
  }

  public void setDevueltos(Long devueltos) {
    this.devueltos = devueltos;
  }

  public Long getRenovados() {
    return renovados;
  }

  public void setRenovados(Long renovados) {
    this.renovados = renovados;
  }

}
