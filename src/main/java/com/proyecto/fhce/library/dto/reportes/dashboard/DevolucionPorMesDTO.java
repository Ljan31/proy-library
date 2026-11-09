package com.proyecto.fhce.library.dto.reportes.dashboard;

public class DevolucionPorMesDTO {
  private Integer anio;
  private Integer mes;
  private Long puntuales;
  private Long tardias;

  public DevolucionPorMesDTO(Integer anio, Integer mes, Long puntuales, Long tardias) {
    this.anio = anio;
    this.mes = mes;
    this.puntuales = puntuales;
    this.tardias = tardias;
  }

  public DevolucionPorMesDTO() {
  }

  public Integer getAnio() {
    return anio;
  }

  public void setAnio(Integer anio) {
    this.anio = anio;
  }

  public Integer getMes() {
    return mes;
  }

  public void setMes(Integer mes) {
    this.mes = mes;
  }

  public Long getPuntuales() {
    return puntuales;
  }

  public void setPuntuales(Long puntuales) {
    this.puntuales = puntuales;
  }

  public Long getTardias() {
    return tardias;
  }

  public void setTardias(Long tardias) {
    this.tardias = tardias;
  }

}
