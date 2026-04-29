package com.proyecto.fhce.library.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Autowired
  private StorageProperties storageProperties;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Convierte ./portadas en ruta absoluta para que Spring la sirva
    storageProperties.getTipos().forEach((key, config) -> {

      Path rutaAbsoluta = Paths.get(config.getRutaBase()).toAbsolutePath();

      registry.addResourceHandler(config.getUrlBase() + "/**")
          .addResourceLocations("file:" + rutaAbsoluta + "/");
    });
  }
}