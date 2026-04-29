package com.proyecto.fhce.library.controllers;

import com.proyecto.fhce.library.dto.request.library.AutorRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.library.AutorResponse;
import com.proyecto.fhce.library.services.library.AutorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autores")
@Tag(name = "Autores", description = "Gestión de autores normalizados")
public class AutorController {

  @Autowired
  private AutorService autorService;

  @Operation(summary = "Listar todos los autores")
  @GetMapping
  public ResponseEntity<ApiResponse<List<AutorResponse>>> findAll() {
    return ResponseEntity.ok(ApiResponse.success(autorService.findAll()));
  }

  @Operation(summary = "Buscar autores por nombre")
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<AutorResponse>>> search(@RequestParam String q) {
    return ResponseEntity.ok(ApiResponse.success(autorService.searchByNombre(q)));
  }

  @Operation(summary = "Obtener autor por ID")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<AutorResponse>> findById(@PathVariable Long id) {
    return ResponseEntity.ok(ApiResponse.success(autorService.findById(id)));
  }

  @Operation(summary = "Crear autor", security = @SecurityRequirement(name = "bearer-jwt"))
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<AutorResponse>> create(@Valid @RequestBody AutorRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Autor creado exitosamente", autorService.create(request)));
  }

  @Operation(summary = "Actualizar autor", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<AutorResponse>> update(
      @PathVariable Long id, @Valid @RequestBody AutorRequest request) {
    return ResponseEntity.ok(ApiResponse.success("Autor actualizado", autorService.update(id, request)));
  }

  @Operation(summary = "Eliminar autor", security = @SecurityRequirement(name = "bearer-jwt"))
  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    autorService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Autor eliminado", null));
  }
}
