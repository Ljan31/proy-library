package com.proyecto.fhce.library.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.fhce.library.dto.request.library.LibroLoteRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.library.LibroLoteResponse;
import com.proyecto.fhce.library.services.library.LibroLoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/catalogo")
@Tag(name = "Catálogo — Carga en lote", description = "Permite registrar un libro completo (obra + ediciones + ejemplares) en una sola llamada.")
public class LibroLoteController {

  @Autowired
  private LibroLoteService libroLoteService;

  @Autowired
  private ObjectMapper objectMapper;

  /**
   * POST /api/catalogo/lote
   *
   * Content-Type: multipart/form-data
   *
   * Partes obligatorias:
   * datos → JSON con LibroLoteRequest (application/json)
   *
   * Partes opcionales (índice = posición de la edición en el array):
   * portada_0 → imagen de portada para la edición 0
   * portada_1 → imagen de portada para la edición 1 (si hay más ediciones)
   * pdf_0 → PDF digital para la edición 0
   * pdf_1 → PDF digital para la edición 1
   *
   * Si no se envía archivo para una edición, se usa imagenPortadaUrl / pdfUrl del
   * JSON.
   * Toda la operación es atómica: si algo falla no se guarda nada.
   */
  @Operation(summary = "Carga en lote: libro + ediciones + ejemplares", description = """
      Registra un libro completo en una sola transacción atómica.

      **Estructura multipart:**
      - `datos` (obligatorio): JSON con el libro, sus ediciones y los ejemplares de cada edición.
      - `portada_N` (opcional): imagen de portada para la edición en posición N del array.
      - `pdf_N` (opcional): PDF digital para la edición en posición N del array.

      **Ejemplo con 2 ediciones:**
      ```
      datos      → { "titulo": "...", "ediciones": [ {...}, {...} ] }
      portada_0  → archivo.jpg  (portada de ediciones[0])
      pdf_0      → libro.pdf    (PDF de ediciones[0])
      portada_1  → archivo2.jpg (portada de ediciones[1])
      ```

      Si la operación falla en cualquier punto, se hace rollback de BD
      y se eliminan los archivos ya guardados en disco.
      """, security = @SecurityRequirement(name = "bearer-jwt"))
  @PostMapping(value = "/lote", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<LibroLoteResponse>> crearEnLote(
      MultipartHttpServletRequest httpRequest) throws IOException {

    // ── 1. Deserializar el JSON de la parte "datos" ───────────────────────
    MultipartFile datosPart = httpRequest.getFile("datos");
    if (datosPart == null || datosPart.isEmpty()) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("La parte 'datos' (JSON) es obligatoria"));
    }

    LibroLoteRequest request;
    try {
      request = objectMapper.readValue(datosPart.getBytes(), LibroLoteRequest.class);
    } catch (IOException e) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("JSON inválido en la parte 'datos': " + e.getMessage()));
    }

    // ── 2. Validar manualmente (equivalente a @Valid en @RequestBody) ─────
    // Si querés usar javax.validation aquí, inyectá Validator y llamá validate()
    if (request.getTitulo() == null || request.getTitulo().isBlank()) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("El título del libro es obligatorio"));
    }
    if (request.getEdiciones() == null || request.getEdiciones().isEmpty()) {
      return ResponseEntity.badRequest()
          .body(ApiResponse.error("Debe incluir al menos una edición"));
    }

    // ── 3. Recolectar todos los archivos del request ──────────────────────
    // Convención: portada_0, portada_1, pdf_0, pdf_1, ...
    Map<String, MultipartFile> archivos = new HashMap<>();
    httpRequest.getFileMap().forEach((nombre, archivo) -> {
      if (!nombre.equals("datos")) {
        archivos.put(nombre, archivo);
      }
    });

    // ── 4. Delegar al service ─────────────────────────────────────────────
    LibroLoteResponse resultado = libroLoteService.crearEnLote(request, archivos);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(
            String.format("Libro creado con %d edición(es) y %d ejemplar(es)",
                resultado.getEdiciones().size(),
                resultado.getTotalEjemplaresCreados()),
            resultado));
  }
}