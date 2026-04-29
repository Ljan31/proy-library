package com.proyecto.fhce.library.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

  private Map<String, TipoStorage> tipos = new HashMap<>();

  public Map<String, TipoStorage> getTipos() {
    return tipos;
  }

  public void setTipos(Map<String, TipoStorage> tipos) {
    this.tipos = tipos;
  }

  public static class TipoStorage {
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
}