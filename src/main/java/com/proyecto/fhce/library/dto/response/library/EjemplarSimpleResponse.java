package com.proyecto.fhce.library.dto.response.library;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

public class EjemplarSimpleResponse {
  private Long id_ejemplar;
  private String codigo_ejemplar;
  private EstadoEjemplar estadoEjemplar;
  private String ubicacion_fisica;

  public Long getId_ejemplar() {
    return id_ejemplar;
  }

  public void setId_ejemplar(Long id_ejemplar) {
    this.id_ejemplar = id_ejemplar;
  }

  public String getCodigo_ejemplar() {
    return codigo_ejemplar;
  }

  public void setCodigo_ejemplar(String codigo_ejemplar) {
    this.codigo_ejemplar = codigo_ejemplar;
  }

  public EstadoEjemplar getEstadoEjemplar() {
    return estadoEjemplar;
  }

  public void setEstadoEjemplar(EstadoEjemplar estadoEjemplar) {
    this.estadoEjemplar = estadoEjemplar;
  }

  public String getUbicacion_fisica() {
    return ubicacion_fisica;
  }

  public void setUbicacion_fisica(String ubicacion_fisica) {
    this.ubicacion_fisica = ubicacion_fisica;
  }

}
