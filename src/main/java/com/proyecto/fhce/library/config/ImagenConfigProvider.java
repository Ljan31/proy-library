package com.proyecto.fhce.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImagenConfigProvider {

  @Value("${app.certificados.imagen.firma.ancho:2160000}")
  private long firmaAncho;
  @Value("${app.certificados.imagen.firma.alto:1080000}")
  private long firmaAlto;

  @Value("${app.certificados.imagen.sello.ancho:1800000}")
  private long selloAncho;
  @Value("${app.certificados.imagen.sello.alto:1800000}")
  private long selloAlto;

  @Value("${app.certificados.imagen.qr.ancho:1800000}")
  private long qrAncho;
  @Value("${app.certificados.imagen.qr.alto:1800000}")
  private long qrAlto;

  public long[] dimensiones(String clave) {
    return switch (clave.toLowerCase()) {
      case "firma" -> new long[] { firmaAncho, firmaAlto };
      case "sello" -> new long[] { selloAncho, selloAlto };
      case "qr" -> new long[] { qrAncho, qrAlto };
      default -> new long[] { 2_520_000L, 1_440_000L };
    };
  }
}
