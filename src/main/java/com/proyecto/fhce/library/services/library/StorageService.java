package com.proyecto.fhce.library.services.library;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.fhce.library.config.StorageProperties;
import com.proyecto.fhce.library.enums.TipoArchivo;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;

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

      case RESPALDOS_ENCARGADOS:

        boolean esPdf = contentType != null &&
            contentType.equals("application/pdf");

        boolean esImagen = contentType != null &&
            contentType.startsWith("image/");

        if (!esPdf && !esImagen) {
          throw new RuntimeException(
              "El respaldo debe ser PDF o imagen");
        }

        break;
      case PLANTILLAS_CERTIFICADO: // ← nuevo case
        boolean esDocx = contentType != null && (contentType.equals("application/vnd.openxmlformats-officedocument" +
            ".wordprocessingml.document") ||
            contentType.equals("application/octet-stream") // algunos OS lo envían así
        );
        // doble check por extensión, el content-type de .docx varía por OS
        String nombre = archivo.getOriginalFilename() != null
            ? archivo.getOriginalFilename()
            : "";
        if (!esDocx && !nombre.toLowerCase().endsWith(".docx"))
          throw new RuntimeException("La plantilla debe ser un archivo .docx");

        break;
      case FIRMAS_CERTIFICADO, SELLOS_CERTIFICADO:
        boolean esImagenC = contentType != null && (contentType.equals("image/png") ||
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/webp"));
        if (!esImagenC)
          throw new RuntimeException("La imagen debe ser PNG, JPG o WEBP");
        if (archivo.getSize() > 2 * 1024 * 1024)
          throw new RuntimeException("La imagen no puede superar 2 MB");

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

  /**
   * Carga un archivo almacenado como bytes en memoria, sin exponerlo por URL.
   * Usado para leer plantillas .docx antes de procesar.
   *
   * @param urlRelativa la URL relativa devuelta por guardar(), ej:
   *                    /plantillas_certificado/uuid.docx
   * @param tipo        tipo de archivo para resolver la ruta base
   */
  public byte[] cargarEnMemoria(String urlRelativa, TipoArchivo tipo) {
    StorageProperties.TipoStorage config = obtenerConfig(tipo);

    if (!urlRelativa.startsWith(config.getUrlBase()))
      throw new RuntimeException("URL no corresponde al tipo: " + tipo);

    String nombreArchivo = urlRelativa.substring(config.getUrlBase().length() + 1);

    Path rutaBase = Paths.get(config.getRutaBase()).toAbsolutePath().normalize();
    Path archivo = rutaBase.resolve(nombreArchivo).normalize();

    // Seguridad: evitar path traversal
    if (!archivo.startsWith(rutaBase))
      throw new RuntimeException("Acceso no permitido a la ruta solicitada");

    try {
      return Files.readAllBytes(archivo);
    } catch (IOException e) {
      throw new RuntimeException("No se pudo leer el archivo: " + urlRelativa, e);
    }
  }

  public Resource cargarComoResource(String urlRelativa, TipoArchivo tipo) {
    StorageProperties.TipoStorage config = obtenerConfig(tipo);

    if (urlRelativa == null || urlRelativa.isBlank()) {
      throw new RuntimeException("La URL del archivo está vacía");
    }

    String urlBase = config.getUrlBase();

    if (!urlRelativa.startsWith(urlBase + "/")) {
      throw new RuntimeException(
          "URL no corresponde al tipo de archivo: " + tipo);
    }

    String nombreArchivo = urlRelativa.substring((urlBase + "/").length());

    Path rutaBase = Paths.get(config.getRutaBase())
        .toAbsolutePath()
        .normalize();

    Path archivo = rutaBase
        .resolve(nombreArchivo)
        .normalize();

    // Seguridad contra path traversal
    if (!archivo.startsWith(rutaBase)) {
      throw new RuntimeException(
          "Acceso no permitido a la ruta solicitada");
    }

    if (!Files.exists(archivo) || !Files.isReadable(archivo)) {
      throw new ResourceNotFoundException(
          "Archivo no encontrado: " + urlRelativa);
    }

    try {
      return new UrlResource(archivo.toUri());
    } catch (MalformedURLException e) {
      throw new RuntimeException(
          "No se pudo crear el recurso: " + urlRelativa,
          e);
    }
  }
}