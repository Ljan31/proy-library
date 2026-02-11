package com.proyecto.fhce.library.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.library.BusquedaLibroRequest;
import com.proyecto.fhce.library.dto.request.library.LibroRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.PageResponse;
import com.proyecto.fhce.library.dto.response.library.LibroResponse;
import com.proyecto.fhce.library.services.library.LibroService;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/libros")
@Tag(name = "Libros", description = "Gestión de libros de libros")
public class LibroController {

  @Autowired
  private LibroService libroService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<LibroResponse>>> findAll() {
    List<LibroResponse> libros = libroService.findAll();
    return ResponseEntity.ok(ApiResponse.success(libros));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<LibroResponse>> findById(@PathVariable Long id) {
    LibroResponse libro = libroService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(libro));
  }

  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<LibroResponse>>> search(@RequestParam String q) {
    List<LibroResponse> libros = libroService.search(q);
    return ResponseEntity.ok(ApiResponse.success(libros));
  }

  @PostMapping("/busqueda-avanzada")
  public ResponseEntity<ApiResponse<PageResponse<LibroResponse>>> busquedaAvanzada(
      @RequestBody BusquedaLibroRequest request,
      Pageable pageable) {

    PageResponse<LibroResponse> resultado = libroService.busquedaAvanzada(request, pageable);

    return ResponseEntity.ok(ApiResponse.success(resultado));
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<LibroResponse>> create(@Valid @RequestBody LibroRequest request) {
    LibroResponse libro = libroService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Libro creado exitosamente", libro));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<LibroResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody LibroRequest request) {
    LibroResponse libro = libroService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Libro actualizado", libro));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    libroService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Libro eliminado", null));
  }
}
