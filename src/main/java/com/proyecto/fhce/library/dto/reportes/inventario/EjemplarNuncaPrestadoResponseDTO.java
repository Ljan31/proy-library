package com.proyecto.fhce.library.dto.reportes.inventario;

import java.util.List;

public class EjemplarNuncaPrestadoResponseDTO {
  private Long totalEjemplaresNuncaPrestados;
  private Long totalEjemplares; // para calcular el porcentaje
  private double porcentajeSinCirculacion;
  private List<EjemplarNuncaPrestadoDTO> ejemplares;

  public Long getTotalEjemplaresNuncaPrestados() {
    return totalEjemplaresNuncaPrestados;
  }

  public void setTotalEjemplaresNuncaPrestados(Long totalEjemplaresNuncaPrestados) {
    this.totalEjemplaresNuncaPrestados = totalEjemplaresNuncaPrestados;
  }

  public Long getTotalEjemplares() {
    return totalEjemplares;
  }

  public void setTotalEjemplares(Long totalEjemplares) {
    this.totalEjemplares = totalEjemplares;
  }

  public double getPorcentajeSinCirculacion() {
    return porcentajeSinCirculacion;
  }

  public void setPorcentajeSinCirculacion(double porcentajeSinCirculacion) {
    this.porcentajeSinCirculacion = porcentajeSinCirculacion;
  }

  public List<EjemplarNuncaPrestadoDTO> getEjemplares() {
    return ejemplares;
  }

  public void setEjemplares(List<EjemplarNuncaPrestadoDTO> ejemplares) {
    this.ejemplares = ejemplares;
  }

}