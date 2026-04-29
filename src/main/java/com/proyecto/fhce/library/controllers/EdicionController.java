package com.proyecto.fhce.library.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.fhce.library.dto.request.library.EdicionRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.library.EdicionResponse;
import com.proyecto.fhce.library.services.library.EdicionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ediciones")
@Tag(name = "Ediciones", description = "Gestión de ediciones de libros")
public class EdicionController {

  @Autowired
  private EdicionService edicionService;

  @Operation(summary = "Listar ediciones de un libro")
  @GetMapping("/libro/{libroId}")
  public ResponseEntity<ApiResponse<List<EdicionResponse>>> findByLibro(@PathVariable Long libroId) {
    return ResponseEntity.ok(ApiResponse.success(edicionService.findByLibro(libroId)));
  }

  @Operation(summary = "Obtener edición por ID")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<EdicionResponse>> findById(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(edicionService.findById(id)));
  }

  @Operation(summary = "Crear nueva edición", description = "Acepta multipart/form-data. La portada puede ser archivo o URL.", security = @SecurityRequirement(name = "bearer-jwt"))
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<EdicionResponse>> create(
      @Valid @RequestPart("datos") EdicionRequest request,
      @RequestPart(value = "portada", required = false) MultipartFile portadaFile,
      @RequestPart(value = "pdf", required = false) MultipartFile pdfFile) {
    EdicionResponse edicion = edicionService.create(request, portadaFile, pdfFile);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Edición creada exitosamente", edicion));
  }

  @Operation(summary = "Actualizar edición", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<EdicionResponse>> update(
      @PathVariable Long id,
      @Valid @RequestPart("datos") EdicionRequest request,
      @RequestPart(value = "portada", required = false) MultipartFile portadaFile,
      @RequestPart(value = "pdf", required = false) MultipartFile pdfFile) {
    EdicionResponse edicion = edicionService.update(id, request, portadaFile, pdfFile);
    return ResponseEntity.ok(ApiResponse.success("Edición actualizada exitosamente", edicion));
  }

  @Operation(summary = "Eliminar edición", security = @SecurityRequirement(name = "bearer-jwt"))
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    edicionService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Edición eliminada exitosamente", null));
  }
}