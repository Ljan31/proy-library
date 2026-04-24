package com.proyecto.fhce.library.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.ReglasPrestamoDTO;
import com.proyecto.fhce.library.dto.request.loads.ConfiguracionPrestamoRequestDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionPrestamoResponseDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionResueltaDTO;
import com.proyecto.fhce.library.enums.TipoPrestamo;
import com.proyecto.fhce.library.services.loads.ConfiguracionPrestamoService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/configuraciones-prestamo")
public class ConfiguracionPrestamoController {

  private final ConfiguracionPrestamoService configuracionService;

  public ConfiguracionPrestamoController(ConfiguracionPrestamoService configuracionService) {
    this.configuracionService = configuracionService;
  }

  @PostMapping
  // @PreAuthorize("hasAuthority('CONFIGURACION_PRESTAMO_CREAR')")
  public ResponseEntity<ConfiguracionPrestamoResponseDTO> crear(
      @Valid @RequestBody ConfiguracionPrestamoRequestDTO request) {

    ConfiguracionPrestamoResponseDTO respuesta = configuracionService.crear(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
  }

  @PutMapping("/{id}")
  // @PreAuthorize("hasAuthority('CONFIGURACION_PRESTAMO_EDITAR')")
  public ResponseEntity<ConfiguracionPrestamoResponseDTO> actualizar(
      @PathVariable Long id,
      @Valid @RequestBody ConfiguracionPrestamoRequestDTO request) {

    return ResponseEntity.ok(configuracionService.actualizar(id, request));
  }

  @DeleteMapping("/{id}")
  // @PreAuthorize("hasAuthority('CONFIGURACION_PRESTAMO_ELIMINAR')")
  public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    configuracionService.eliminar(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<ConfiguracionPrestamoResponseDTO> buscarPorId(@PathVariable Long id) {
    return ResponseEntity.ok(configuracionService.buscarPorId(id));
  }

  @GetMapping("/biblioteca/{bibliotecaId}")
  public ResponseEntity<ConfiguracionPrestamoResponseDTO> listarPorBiblioteca(
      @PathVariable Long bibliotecaId) {

    return ResponseEntity.ok(configuracionService.buscarPorBiblioteca(bibliotecaId));
  }

  // Endpoint para que otros módulos obtengan las reglas según tipo de préstamo
  @GetMapping("/biblioteca/{bibliotecaId}/reglas")
  public ResponseEntity<ReglasPrestamoDTO> obtenerReglas(
      @PathVariable Long bibliotecaId,
      @RequestParam TipoPrestamo tipoPrestamo) {

    return ResponseEntity.ok(
        configuracionService.obtenerReglas(bibliotecaId, tipoPrestamo));
  }

  /**
   * Calcula la multa para un préstamo vencido.
   * Puede llamarlo el módulo de sanciones directamente.
   */
  @GetMapping("/{id}/calcular-multa")
  public ResponseEntity<BigDecimal> calcularMulta(
      @PathVariable Long id,
      @RequestParam @Min(1) int diasRetraso) {

    return ResponseEntity.ok(configuracionService.calcularMulta(id, diasRetraso));
  }
}