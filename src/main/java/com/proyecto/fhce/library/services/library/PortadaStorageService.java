package com.proyecto.fhce.library.services.library;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.fhce.library.config.PortadasProperties;
import com.proyecto.fhce.library.exception.BusinessException;

@Service
public class PortadaStorageService {

  private final Path rutaBase;
  private final String urlBase;

  public PortadaStorageService(PortadasProperties props) {
    this.rutaBase = Paths.get(props.getRutaBase()).toAbsolutePath().normalize();
    this.urlBase = props.getUrlBase();

    // Crear el directorio si no existe al arrancar
    try {
      Files.createDirectories(this.rutaBase);
    } catch (IOException e) {
      throw new RuntimeException("No se pudo crear el directorio de portadas: " + rutaBase, e);
    }
  }

  /**
   * Guarda el archivo y devuelve la URL relativa lista para guardar en BD.
   * Ej: /portadas/a3f8b1c2-clean-code.jpg
   */
  public String guardar(MultipartFile archivo) {
    validarArchivo(archivo);

    String nombreFinal = generarNombre(archivo.getOriginalFilename());
    Path destino = rutaBase.resolve(nombreFinal);

    try {
      Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new RuntimeException("Error al guardar la portada: " + nombreFinal, e);
    }

    return urlBase + "/" + nombreFinal;
  }

  public void eliminar(String urlRelativa) {
    if (urlRelativa == null || !urlRelativa.startsWith(urlBase))
      return;

    String nombreArchivo = urlRelativa.substring(urlBase.length() + 1);
    Path archivo = rutaBase.resolve(nombreArchivo);

    try {
      Files.deleteIfExists(archivo);
    } catch (IOException e) {
      // Log pero no falla — el registro en BD puede borrarse igual
    }
  }

  private void validarArchivo(MultipartFile archivo) {
    if (archivo == null || archivo.isEmpty()) {
      throw new BusinessException("El archivo de portada está vacío");
    }

    String contentType = archivo.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new BusinessException("El archivo debe ser una imagen (jpg, png, webp)");
    }

    // 5 MB máximo
    if (archivo.getSize() > 5 * 1024 * 1024) {
      throw new BusinessException("La imagen no puede superar los 5 MB");
    }
  }

  private String generarNombre(String nombreOriginal) {
    String extension = "";
    if (nombreOriginal != null && nombreOriginal.contains(".")) {
      extension = nombreOriginal.substring(nombreOriginal.lastIndexOf(".")).toLowerCase();
    }
    // UUID para evitar colisiones y nombres conflictivos
    return UUID.randomUUID().toString() + extension;
  }
}