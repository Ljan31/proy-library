package com.proyecto.fhce.library.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.proyecto.fhce.library.dto.request.library.CategoriaLibroRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.library.CategoriaDetalleResponse;
import com.proyecto.fhce.library.dto.response.library.CategoriaLibroResponse;
import com.proyecto.fhce.library.dto.response.library.EstadisticasCategoriasResponse;
import com.proyecto.fhce.library.services.library.CategoriaLibroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorías", description = "Gestión de categorías de libros")
public class CategoriaLibroController {
  @Autowired
  private CategoriaLibroService categoriaService;

  @Operation(summary = "Listar todas las categorías", description = "Obtiene el listado completo de categorías de libros")
  @GetMapping
  public ResponseEntity<ApiResponse<List<CategoriaLibroResponse>>> findAll() {
    List<CategoriaLibroResponse> categorias = categoriaService.findAll();
    return ResponseEntity.ok(ApiResponse.success(categorias));
  }

  @Operation(summary = "Listar categorías ordenadas", description = "Obtiene categorías ordenadas alfabéticamente")
  @GetMapping("/ordenadas")
  public ResponseEntity<ApiResponse<List<CategoriaLibroResponse>>> findAllOrdenadas() {
    List<CategoriaLibroResponse> categorias = categoriaService.findAllOrdenadas();
    return ResponseEntity.ok(ApiResponse.success(categorias));
  }

  @Operation(summary = "Listar categorías con libros", description = "Obtiene categorías ordenadas por cantidad de libros (descendente)")
  @GetMapping("/con-libros")
  public ResponseEntity<ApiResponse<List<CategoriaLibroResponse>>> findCategoriasConLibros() {
    List<CategoriaLibroResponse> categorias = categoriaService.findCategoriasConLibros();
    return ResponseEntity.ok(ApiResponse.success(categorias));
  }

  @Operation(summary = "Obtener categoría por ID", description = "Retorna la información de una categoría específica")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoriaLibroResponse>> findById(@PathVariable Long id) {
    CategoriaLibroResponse categoria = categoriaService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(categoria));
  }

  @Operation(summary = "Obtener categoría con detalles", description = "Retorna la categoría con sus libros asociados")
  @GetMapping("/{id}/detalle")
  public ResponseEntity<ApiResponse<CategoriaDetalleResponse>> findByIdWithDetalle(@PathVariable Long id) {
    CategoriaDetalleResponse detalle = categoriaService.findByIdWithDetalle(id);
    return ResponseEntity.ok(ApiResponse.success(detalle));
  }

  @Operation(summary = "Buscar categoría por código Dewey", description = "Busca una categoría por su código de clasificación Dewey")
  @GetMapping("/dewey/{codigo}")
  public ResponseEntity<ApiResponse<CategoriaLibroResponse>> findByCodigoDewey(@PathVariable String codigo) {
    CategoriaLibroResponse categoria = categoriaService.findByCodigoDewey(codigo);
    return ResponseEntity.ok(ApiResponse.success(categoria));
  }

  @Operation(summary = "Buscar categorías", description = "Busca categorías por nombre")
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<CategoriaLibroResponse>>> search(@RequestParam String q) {
    List<CategoriaLibroResponse> categorias = categoriaService.search(q);
    return ResponseEntity.ok(ApiResponse.success(categorias));
  }

  @Operation(summary = "Obtener estadísticas de categorías", description = "Retorna estadísticas generales de categorías")
  @GetMapping("/estadisticas")
  public ResponseEntity<ApiResponse<EstadisticasCategoriasResponse>> getEstadisticas() {
    EstadisticasCategoriasResponse stats = categoriaService.obtenerEstadisticas();
    return ResponseEntity.ok(ApiResponse.success(stats));
  }

  @Operation(summary = "Obtener distribución por rangos Dewey", description = "Retorna la distribución de categorías según clasificación Dewey")
  @GetMapping("/distribucion-dewey")
  public ResponseEntity<ApiResponse<Map<String, Integer>>> getDistribucionDewey() {
    Map<String, Integer> distribucion = categoriaService.obtenerDistribucionPorRangoDewey();
    return ResponseEntity.ok(ApiResponse.success(distribucion));
  }

  @Operation(summary = "Crear nueva categoría", description = "Registra una nueva categoría de libros", security = @SecurityRequirement(name = "bearer-jwt"))
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<CategoriaLibroResponse>> create(
      @Valid @RequestBody CategoriaLibroRequest request) {
    CategoriaLibroResponse categoria = categoriaService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Categoría creada exitosamente", categoria));
  }

  @Operation(summary = "Actualizar categoría", description = "Actualiza la información de una categoría existente", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<CategoriaLibroResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody CategoriaLibroRequest request) {
    CategoriaLibroResponse categoria = categoriaService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Categoría actualizada exitosamente", categoria));
  }

  @Operation(summary = "Eliminar categoría", description = "Elimina una categoría (solo si no tiene libros asociados)", security = @SecurityRequirement(name = "bearer-jwt"))
  @DeleteMapping("/{id}")
  @PreAuthorize(" hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    categoriaService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Categoría eliminada exitosamente", null));
  }
}
