package com.proyecto.fhce.library.dto.reportes.inventario;

public class EjemplarNuncaPrestadoDTO {
  private Long idEjemplar;
  private String codigoEjemplar;
  private String libro;
  private String editorial;
  private String biblioteca;
  private String clasificacionDecimal;
  private String ubicacionFisica;
  private String estadoEjemplar;

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

  public String getLibro() {
    return libro;
  }

  public void setLibro(String libro) {
    this.libro = libro;
  }

  public String getEditorial() {
    return editorial;
  }

  public void setEditorial(String editorial) {
    this.editorial = editorial;
  }

  public String getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(String biblioteca) {
    this.biblioteca = biblioteca;
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

  public String getEstadoEjemplar() {
    return estadoEjemplar;
  }

  public void setEstadoEjemplar(String estadoEjemplar) {
    this.estadoEjemplar = estadoEjemplar;
  }

}