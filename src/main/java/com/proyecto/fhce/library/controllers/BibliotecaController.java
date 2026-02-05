package com.proyecto.fhce.library.controllers;

import java.util.List;

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

import com.proyecto.fhce.library.dto.request.BibliotecaRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.BibliotecaResponse;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.services.library.BibliotecaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bibliotecas")
@Tag(name = "Bibliotecas", description = "Gestión de bibliotecas")
public class BibliotecaController {
  @Autowired
  private BibliotecaService bibliotecaService;

  @Operation(summary = "Listar todas las bibliotecas", description = "Obtiene el listado completo de bibliotecas")
  @GetMapping
  public ResponseEntity<ApiResponse<List<BibliotecaResponse>>> findAll() {
    List<BibliotecaResponse> bibliotecas = bibliotecaService.findAll();
    return ResponseEntity.ok(ApiResponse.success(bibliotecas));
  }

  @Operation(summary = "Listar bibliotecas activas", description = "Obtiene solo las bibliotecas con estado ACTIVA")
  @GetMapping("/activas")
  public ResponseEntity<ApiResponse<List<BibliotecaResponse>>> findActivas() {
    List<BibliotecaResponse> bibliotecas = bibliotecaService.findActivas();
    return ResponseEntity.ok(ApiResponse.success(bibliotecas));
  }

  @Operation(summary = "Obtener biblioteca por ID", description = "Retorna la información detallada de una biblioteca")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<BibliotecaResponse>> findById(@PathVariable Long id) {
    BibliotecaResponse biblioteca = bibliotecaService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(biblioteca));
  }

  @Operation(summary = "Obtener biblioteca facultativa", description = "Retorna la biblioteca facultativa principal")
  @GetMapping("/facultativa")
  public ResponseEntity<ApiResponse<BibliotecaResponse>> findFacultativa() {
    BibliotecaResponse biblioteca = bibliotecaService.findBibliotecaFacultativa();
    return ResponseEntity.ok(ApiResponse.success(biblioteca));
  }

  @Operation(summary = "Filtrar bibliotecas por tipo", description = "Obtiene bibliotecas filtradas por tipo (CARRERA o FACULTATIVA)")
  @GetMapping("/tipo/{tipo}")
  public ResponseEntity<ApiResponse<List<BibliotecaResponse>>> findByTipo(
      @PathVariable TipoBiblioteca tipo) {
    List<BibliotecaResponse> bibliotecas = bibliotecaService.findByTipo(tipo);
    return ResponseEntity.ok(ApiResponse.success(bibliotecas));
  }

  @Operation(summary = "Obtener bibliotecas por carrera", description = "Lista las bibliotecas asociadas a una carrera")
  @GetMapping("/carrera/{carreraId}")
  public ResponseEntity<ApiResponse<List<BibliotecaResponse>>> findByCarrera(
      @PathVariable Long carreraId) {
    List<BibliotecaResponse> bibliotecas = bibliotecaService.findByCarrera(carreraId);
    return ResponseEntity.ok(ApiResponse.success(bibliotecas));
  }

  @Operation(summary = "Buscar bibliotecas", description = "Busca bibliotecas por nombre")
  @GetMapping("/search")
  public ResponseEntity<ApiResponse<List<BibliotecaResponse>>> search(@RequestParam String q) {
    List<BibliotecaResponse> bibliotecas = bibliotecaService.search(q);
    return ResponseEntity.ok(ApiResponse.success(bibliotecas));
  }

  // @Operation(summary = "Obtener estadísticas de biblioteca", description =
  // "Retorna estadísticas detalladas de una biblioteca", security =
  // @SecurityRequirement(name = "bearer-jwt"))
  // @GetMapping("/{id}/estadisticas")
  // @PreAuthorize("hasAuthority('BIBLIOTECAS_VER') or hasRole('ADMIN') or
  // hasRole('BIBLIOTECARIO')")
  // public ResponseEntity<ApiResponse<EstadisticasBibliotecaResponse>>
  // getEstadisticas(
  // @PathVariable Long id) {
  // EstadisticasBibliotecaResponse stats =
  // bibliotecaService.obtenerEstadisticas(id);
  // return ResponseEntity.ok(ApiResponse.success(stats));
  // }

  // @Operation(summary = "Obtener inventario de biblioteca", description =
  // "Retorna el inventario detallado por estado de ejemplares", security =
  // @SecurityRequirement(name = "bearer-jwt"))
  // @GetMapping("/{id}/inventario")
  // @PreAuthorize("hasAuthority('BIBLIOTECAS_VER') or hasRole('ADMIN') or
  // hasRole('BIBLIOTECARIO')")
  // public ResponseEntity<ApiResponse<InventarioBibliotecaResponse>>
  // getInventario(
  // @PathVariable Long id) {
  // InventarioBibliotecaResponse inventario =
  // bibliotecaService.obtenerInventario(id);
  // return ResponseEntity.ok(ApiResponse.success(inventario));
  // }

  @Operation(summary = "Crear nueva biblioteca", description = "Registra una nueva biblioteca en el sistema", security = @SecurityRequirement(name = "bearer-jwt"))
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<BibliotecaResponse>> create(
      @Valid @RequestBody BibliotecaRequest request) {
    BibliotecaResponse biblioteca = bibliotecaService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Biblioteca creada exitosamente", biblioteca));
  }

  @Operation(summary = "Actualizar biblioteca", description = "Actualiza la información de una biblioteca existente", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<BibliotecaResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody BibliotecaRequest request) {
    BibliotecaResponse biblioteca = bibliotecaService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Biblioteca actualizada exitosamente", biblioteca));
  }

  @Operation(summary = "Cambiar estado de biblioteca", description = "Cambia el estado de una biblioteca (ACTIVA, INACTIVA, EN_MANTENIMIENTO)", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{id}/estado")
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<Void>> cambiarEstado(
      @PathVariable Long id,
      @RequestParam EstadoBiblioteca nuevoEstado) {
    bibliotecaService.cambiarEstado(id, nuevoEstado);
    return ResponseEntity.ok(ApiResponse.success("Estado actualizado exitosamente", null));
  }

  @Operation(summary = "Asignar encargado a biblioteca", description = "Asigna un usuario como encargado de la biblioteca", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{bibliotecaId}/encargado/{usuarioId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> asignarEncargado(
      @PathVariable Long bibliotecaId,
      @PathVariable Long usuarioId) {
    bibliotecaService.asignarEncargado(bibliotecaId, usuarioId);
    return ResponseEntity.ok(ApiResponse.success("Encargado asignado exitosamente", null));
  }

  @Operation(summary = "Eliminar biblioteca", description = "Elimina una biblioteca (solo si no tiene ejemplares)", security = @SecurityRequirement(name = "bearer-jwt"))
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('ADMIN','BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    bibliotecaService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Biblioteca eliminada exitosamente", null));
  }
}
