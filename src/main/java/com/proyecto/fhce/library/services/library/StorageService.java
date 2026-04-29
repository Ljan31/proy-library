package com.proyecto.fhce.library.services.library;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.fhce.library.config.StorageProperties;
import com.proyecto.fhce.library.enums.TipoArchivo;

@Service
public class StorageService {

  private final StorageProperties properties;

  public StorageService(StorageProperties properties) {
    this.properties = properties;
  }

  public String guardar(TipoArchivo tipo, MultipartFile archivo) {
    StorageProperties.TipoStorage config = obtenerConfig(tipo);

    validarArchivo(archivo, tipo);

    Path rutaBase = Paths.get(config.getRutaBase()).toAbsolutePath().normalize();
    crearDirectorioSiNoExiste(rutaBase);

    String nombreFinal = generarNombre(archivo.getOriginalFilename());
    Path destino = rutaBase.resolve(nombreFinal);

    try {
      Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Error al guardar archivo", e);
    }

    return config.getUrlBase() + "/" + nombreFinal;
  }

  private StorageProperties.TipoStorage obtenerConfig(TipoArchivo tipo) {
    String clave = tipo.name().toLowerCase();
    StorageProperties.TipoStorage config = properties.getTipos().get(clave);
    if (config == null) {
      throw new RuntimeException("Tipo de storage no configurado: " + tipo);
    }
    return config;
  }

  private void crearDirectorioSiNoExiste(Path ruta) {
    try {
      Files.createDirectories(ruta);
    } catch (IOException e) {
      throw new RuntimeException("No se pudo crear el directorio", e);
    }
  }

  private void validarArchivo(MultipartFile archivo, TipoArchivo tipo) {
    if (archivo == null || archivo.isEmpty()) {
      throw new RuntimeException("Archivo vacío");
    }

    String contentType = archivo.getContentType();

    switch (tipo) {
      case PORTADAS:
      case LOGOS:
        if (contentType == null || !contentType.startsWith("image/")) {
          throw new RuntimeException("Debe ser una imagen");
        }
        break;

      case PDFS:
        if (contentType == null || !contentType.equals("application/pdf")) {
          throw new RuntimeException("Debe ser un PDF");
        }
        break;
    }
  }

  public void eliminar(String urlRelativa, TipoArchivo tipo) {
    if (urlRelativa == null)
      return;

    StorageProperties.TipoStorage config = obtenerConfig(tipo);

    if (!urlRelativa.startsWith(config.getUrlBase()))
      return;

    String nombreArchivo = urlRelativa.substring(config.getUrlBase().length() + 1);
    Path rutaBase = Paths.get(config.getRutaBase()).toAbsolutePath().normalize();
    Path archivo = rutaBase.resolve(nombreArchivo);

    try {
      Files.deleteIfExists(archivo);
    } catch (IOException e) {
      // log opcional
    }
  }

  private String generarNombre(String nombreOriginal) {
    String extension = "";
    if (nombreOriginal != null && nombreOriginal.contains(".")) {
      extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".")).toLowerCase();
    }
    return UUID.randomUUID().toString() + extension;
  }
}