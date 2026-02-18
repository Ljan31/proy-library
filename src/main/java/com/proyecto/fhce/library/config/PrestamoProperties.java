package com.proyecto.fhce.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "prestamo")
public class PrestamoProperties {

  private int diasMaximos;
  private int renovacionesMaximas;
  private int ejemplaresSimultaneosMaximos;

  public int getDiasMaximos() {
    return diasMaximos;
  }

  public void setDiasMaximos(int diasMaximos) {
    this.diasMaximos = diasMaximos;
  }

  public int getRenovacionesMaximas() {
    return renovacionesMaximas;
  }

  public void setRenovacionesMaximas(int renovacionesMaximas) {
    this.renovacionesMaximas = renovacionesMaximas;
  }

  public int getEjemplaresSimultaneosMaximos() {
    return ejemplaresSimultaneosMaximos;
  }

  public void setEjemplaresSimultaneosMaximos(int ejemplaresSimultaneosMaximos) {
    this.ejemplaresSimultaneosMaximos = ejemplaresSimultaneosMaximos;
  }

}
