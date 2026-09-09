package com.proyecto.fhce.library.dto.reportes.inventario;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

public class InventarioItemDTO {
  private Long idEjemplar;

  private String codigoEjemplar;

  private String tituloLibro;

  private String biblioteca;

  private EstadoEjemplar estado;

  private String clasificacionDecimal;

  private String ubicacionFisica;

  public InventarioItemDTO() {
  }

  public Long getIdEjemplar() {
    return idEjemplar;
  }

  public void setIdEjemplar(Long idEjemplar) {
    this.idEjemplar = idEjemplar;
  }

  public String getCodigoEjemplar() {
    return codigoEjemplar;
  }

  public void setCodigoEjemplar(String codigoEjemplar) {
    this.codigoEjemplar = codigoEjemplar;
  }

  public String getTituloLibro() {
    return tituloLibro;
  }

  public void setTituloLibro(String tituloLibro) {
    this.tituloLibro = tituloLibro;
  }

  public String getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(String biblioteca) {
    this.biblioteca = biblioteca;
  }

  public EstadoEjemplar getEstado() {
    return estado;
  }

  public void setEstado(EstadoEjemplar estado) {
    this.estado = estado;
  }

  public String getClasificacionDecimal() {
    return clasificacionDecimal;
  }

  public void setClasificacionDecimal(String clasificacionDecimal) {
    this.clasificacionDecimal = clasificacionDecimal;
  }

  public String getUbicacionFisica() {
    return ubicacionFisica;
  }

  public void setUbicacionFisica(String ubicacionFisica) {
    this.ubicacionFisica = ubicacionFisica;
  }

}
