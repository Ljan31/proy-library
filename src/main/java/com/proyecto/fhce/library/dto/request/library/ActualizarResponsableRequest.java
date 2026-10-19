package com.proyecto.fhce.library.dto.request.library;

public class ActualizarResponsableRequest {
  private String cargoResponsable;
  private String nombreResponsable;

  public String getCargoResponsable() {
    return cargoResponsable;
  }

  public void setCargoResponsable(String cargoResponsable) {
    this.cargoResponsable = cargoResponsable;
  }

  public String getNombreResponsable() {
    return nombreResponsable;
  }

  public void setNombreResponsable(String nombreResponsable) {
    this.nombreResponsable = nombreResponsable;
  }
}
