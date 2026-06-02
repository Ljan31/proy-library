package com.proyecto.fhce.library.dto.reportes.inventario;

import java.util.List;

public class EstadoEjemplarBibliotecaResponseDTO {
  private ResumenEstadoEjemplarDTO resumen;

  private List<EstadoEjemplarBibliotecaDTO> bibliotecas;

  public EstadoEjemplarBibliotecaResponseDTO() {
  }

  public ResumenEstadoEjemplarDTO getResumen() {
    return resumen;
  }

  public void setResumen(ResumenEstadoEjemplarDTO resumen) {
    this.resumen = resumen;
  }

  public List<EstadoEjemplarBibliotecaDTO> getBibliotecas() {
    return bibliotecas;
  }

  public void setBibliotecas(List<EstadoEjemplarBibliotecaDTO> bibliotecas) {
    this.bibliotecas = bibliotecas;
  }

}
