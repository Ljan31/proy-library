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
  private PortadasProperties portadasProperties;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    // Convierte ./portadas en ruta absoluta para que Spring la sirva
    Path rutaAbsoluta = Paths.get(portadasProperties.getRutaBase()).toAbsolutePath();

    registry.addResourceHandler(portadasProperties.getUrlBase() + "/**")
        .addResourceLocations("file:" + rutaAbsoluta + "/");
  }
}