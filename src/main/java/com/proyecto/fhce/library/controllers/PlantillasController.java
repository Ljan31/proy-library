package com.proyecto.fhce.library.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.library.FormatoCertificadoResponse;
import com.proyecto.fhce.library.entities.FormatoCertificado;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.services.library.FormatoCertificadoService;

@RestController
@RequestMapping("/api/plantillas")
public class PlantillasController {
  @Autowired
  private FormatoCertificadoService formatoService;

  @GetMapping("/biblioteca/{bibliotecaId}")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<FormatoCertificadoResponse>> obtenerPlantilla(
      @PathVariable Long bibliotecaId) {

    FormatoCertificadoResponse plantilla = formatoService.obtenerActivo(bibliotecaId);
    return ResponseEntity.ok(ApiResponse.success(plantilla));
  }

  @GetMapping("/biblioteca/{bibliotecaId}/historial")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<FormatoCertificadoResponse>>> historialPlantillas(
      @PathVariable Long bibliotecaId) {

    List<FormatoCertificadoResponse> historial = formatoService.historial(bibliotecaId);
    return ResponseEntity.ok(ApiResponse.success(historial));
  }

  @PostMapping("/biblioteca/{bibliotecaId}")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<FormatoCertificadoResponse>> subirPlantilla(
      @PathVariable Long bibliotecaId,
      @RequestParam("archivo") MultipartFile archivo,
      Authentication authentication) throws IOException {

    Long solicitanteId = obtenerUsuarioId(authentication);
    FormatoCertificadoResponse resultado = formatoService
        .subirPlantilla(bibliotecaId, archivo, solicitanteId, authentication.getAuthorities());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Plantilla actualizada correctamente", resultado));
  }

  // ── POST /certificados/plantilla/imagen ───────────────────────────────
  // Sube firma o sello — el frontend manda tipo = "firma" | "sello"
  @PostMapping("/api/certificados/plantilla/imagen")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<FormatoCertificadoResponse> subirImagen(
      @RequestParam("file") MultipartFile file,
      @RequestParam("bibliotecaId") Long bibliotecaId,
      @RequestParam("tipo") String tipo,
      Authentication authentication) {

    Long usuarioId = obtenerUsuarioId(authentication);
    FormatoCertificadoResponse resultado = formatoService
        .subirImagen(bibliotecaId, tipo, file, usuarioId, authentication.getAuthorities());

    return ResponseEntity.ok(resultado);
  }

  @GetMapping("/biblioteca/{id}/download")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Resource> download(
      @PathVariable Long id,
      Authentication authentication) {

    FormatoCertificado formato = formatoService.obtenerPorId(id);

    Resource resource = formatoService.obtenerArchivo(id);

    String contentType = determinarContentType(
        formato.getNombreArchivo());

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" +
                formato.getNombreArchivo() +
                "\"")
        .body(resource);
  }

  private String determinarContentType(String nombreArchivo) {

    if (nombreArchivo == null) {
      return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    String nombre = nombreArchivo.toLowerCase();

    if (nombre.endsWith(".pdf")) {
      return MediaType.APPLICATION_PDF_VALUE;
    }

    if (nombre.endsWith(".docx")) {
      return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }

    if (nombre.endsWith(".doc")) {
      return "application/msword";
    }

    if (nombre.endsWith(".png")) {
      return MediaType.IMAGE_PNG_VALUE;
    }

    if (nombre.endsWith(".jpg") ||
        nombre.endsWith(".jpeg")) {
      return MediaType.IMAGE_JPEG_VALUE;
    }

    return MediaType.APPLICATION_OCTET_STREAM_VALUE;
  }

  private Long obtenerUsuarioId(Authentication authentication) {
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    return userDetails.getId();
  }
}
