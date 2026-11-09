package com.proyecto.fhce.library.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj inyectable. Permite fijar la fecha en los tests y controlar la zona
 * horaria desde un solo lugar. Si el proyecto ya define un bean Clock,
 * NO copiar este archivo.
 */
@Configuration
public class ClockConfig {

  @Bean
  public Clock clock() {
    return Clock.systemDefaultZone();
  }
}