package com.proyecto.fhce.library.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.library.EjemplarRequest;
import com.proyecto.fhce.library.dto.request.library.EjemplarUpdateEstadoRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.library.DisponibilidadLibroResponse;
import com.proyecto.fhce.library.dto.response.library.EjemplarResponse;
import com.proyecto.fhce.library.dto.response.loads.HistorialEstadoResponse;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.services.library.EjemplarService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ejemplares")
@Tag(name = "Ejemplares", description = "Gestión de ejemplares físicos de libros")
public class EjemplarController {

  @Autowired
  private EjemplarService ejemplarService;

  @Operation(summary = "Listar todos los ejemplares", description = "Obtiene el listado completo de ejemplares", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<EjemplarResponse>>> findAll() {
    List<EjemplarResponse> ejemplares = ejemplarService.findAll();
    return ResponseEntity.ok(ApiResponse.success(ejemplares));
  }

  @Operation(summary = "Obtener ejemplar por ID", description = "Retorna la información detallada de un ejemplar")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<EjemplarResponse>> findById(@PathVariable Long id) {
    EjemplarResponse ejemplar = ejemplarService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(ejemplar));
  }

  @Operation(summary = "Buscar ejemplar por código", description = "Busca un ejemplar por su código único")
  @GetMapping("/codigo/{codigo}")
  public ResponseEntity<ApiResponse<EjemplarResponse>> findByCodigo(@PathVariable String codigo) {
    EjemplarResponse ejemplar = ejemplarService.findByCodigo(codigo);
    return ResponseEntity.ok(ApiResponse.success(ejemplar));
  }

  @Operation(summary = "Obtener ejemplares de un libro", description = "Lista todos los ejemplares físicos de un libro")
  @GetMapping("/libro/{libroId}")
  public ResponseEntity<ApiResponse<List<EjemplarResponse>>> findByLibro(@PathVariable Long libroId) {
    List<EjemplarResponse> ejemplares = ejemplarService.findByLibro(libroId);
    return ResponseEntity.ok(ApiResponse.success(ejemplares));
  }

  @Operation(summary = "Obtener ejemplares de una biblioteca", description = "Lista todos los ejemplares de una biblioteca", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetMapping("/biblioteca/{bibliotecaId}")
  @PreAuthorize("hasAuthority('EJEMPLARES_VER') or hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<EjemplarResponse>>> findByBiblioteca(@PathVariable Long bibliotecaId) {
    List<EjemplarResponse> ejemplares = ejemplarService.findByBiblioteca(bibliotecaId);
    return ResponseEntity.ok(ApiResponse.success(ejemplares));
  }

  @Operation(summary = "Filtrar ejemplares por estado", description = "Obtiene ejemplares filtrados por estado", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetMapping("/estado/{estado}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<EjemplarResponse>>> findByEstado(@PathVariable EstadoEjemplar estado) {
    List<EjemplarResponse> ejemplares = ejemplarService.findByEstado(estado);
    return ResponseEntity.ok(ApiResponse.success(ejemplares));
  }

  @Operation(summary = "Obtener ejemplares disponibles de un libro", description = "Lista solo los ejemplares disponibles para préstamo de un libro")
  @GetMapping("/libro/{libroId}/disponibles")
  public ResponseEntity<ApiResponse<List<EjemplarResponse>>> findDisponiblesByLibro(@PathVariable Long libroId) {
    List<EjemplarResponse> ejemplares = ejemplarService.findDisponiblesByLibro(libroId);
    return ResponseEntity.ok(ApiResponse.success(ejemplares));
  }

  @Operation(summary = "Verificar disponibilidad de un libro", description = "Retorna la disponibilidad del libro en todas las bibliotecas")
  @GetMapping("/libro/{libroId}/disponibilidad")
  public ResponseEntity<ApiResponse<DisponibilidadLibroResponse>> verificarDisponibilidad(@PathVariable Long libroId) {
    DisponibilidadLibroResponse disponibilidad = ejemplarService.verificarDisponibilidad(libroId);
    return ResponseEntity.ok(ApiResponse.success(disponibilidad));
  }

  @Operation(summary = "Obtener historial de estados", description = "Retorna el historial completo de cambios de estado de un ejemplar", security = @SecurityRequirement(name = "bearer-jwt"))
  @GetMapping("/{id}/historial")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<HistorialEstadoResponse>>> getHistorial(@PathVariable Long id) {
    List<HistorialEstadoResponse> historial = ejemplarService.obtenerHistorial(id);
    return ResponseEntity.ok(ApiResponse.success(historial));
  }

  @Operation(summary = "Crear nuevo ejemplar", description = "Registra un nuevo ejemplar físico de un libro", security = @SecurityRequirement(name = "bearer-jwt"))
  @PostMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<EjemplarResponse>> create(@Valid @RequestBody EjemplarRequest request) {
    EjemplarResponse ejemplar = ejemplarService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Ejemplar creado exitosamente", ejemplar));
  }

  @Operation(summary = "Actualizar ejemplar", description = "Actualiza la información de un ejemplar existente", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<EjemplarResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody EjemplarRequest request) {
    EjemplarResponse ejemplar = ejemplarService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Ejemplar actualizado exitosamente", ejemplar));
  }

  @Operation(summary = "Actualizar estado del ejemplar", description = "Cambia el estado de un ejemplar (DISPONIBLE, PRESTADO, EN_REPARACION, etc.)", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{id}/estado")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<EjemplarResponse>> actualizarEstado(
      @PathVariable Long id,
      @Valid @RequestBody EjemplarUpdateEstadoRequest request) {
    EjemplarResponse ejemplar = ejemplarService.actualizarEstado(id, request);
    return ResponseEntity.ok(ApiResponse.success("Estado actualizado exitosamente", ejemplar));
  }

  @Operation(summary = "Dar de baja ejemplar", description = "Marca un ejemplar como dado de baja", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{id}/baja")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> darDeBaja(
      @PathVariable Long id,
      @RequestParam String motivo) {
    ejemplarService.darDeBaja(id, motivo);
    return ResponseEntity.ok(ApiResponse.success("Ejemplar dado de baja exitosamente", null));
  }

  @Operation(summary = "Reportar ejemplar perdido", description = "Marca un ejemplar como perdido", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{id}/perdido")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<Void>> reportarPerdido(
      @PathVariable Long id,
      @RequestParam String motivo) {
    ejemplarService.reportarPerdido(id, motivo);
    return ResponseEntity.ok(ApiResponse.success("Ejemplar reportado como perdido", null));
  }

  @Operation(summary = "Transferir ejemplar a otra biblioteca", description = "Transfiere un ejemplar de una biblioteca a otra", security = @SecurityRequirement(name = "bearer-jwt"))
  @PutMapping("/{ejemplarId}/transferir/{nuevaBibliotecaId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> transferirBiblioteca(
      @PathVariable Long ejemplarId,
      @PathVariable Long nuevaBibliotecaId,
      @RequestParam String motivo) {
    ejemplarService.transferirBiblioteca(ejemplarId, nuevaBibliotecaId, motivo);
    return ResponseEntity.ok(ApiResponse.success("Ejemplar transferido exitosamente", null));
  }
}