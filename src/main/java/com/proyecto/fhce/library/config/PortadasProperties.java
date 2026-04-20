package com.proyecto.fhce.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.portadas")
public class PortadasProperties {

  private String rutaBase;
  private String urlBase;

  public String getRutaBase() {
    return rutaBase;
  }

  public void setRutaBase(String rutaBase) {
    this.rutaBase = rutaBase;
  }

  public String getUrlBase() {
    return urlBase;
  }

  public void setUrlBase(String urlBase) {
    this.urlBase = urlBase;
  }

}