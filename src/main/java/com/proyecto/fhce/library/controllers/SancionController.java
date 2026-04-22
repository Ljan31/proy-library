package com.proyecto.fhce.library.controllers;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.proyecto.fhce.library.dto.SancionDTO.CondonacionRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.EstadoSancionUsuarioDTO;
import com.proyecto.fhce.library.dto.SancionDTO.PagoMultaRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.SancionManualRequestDTO;
import com.proyecto.fhce.library.dto.SancionDTO.SancionResponseDTO;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.services.loads.SancionService;

import java.util.List;

@RestController
@RequestMapping("/api/sanciones")
public class SancionController {

  private final SancionService sancionService;

  public SancionController(SancionService sancionService) {
    this.sancionService = sancionService;
  }

  // ════════════════════════════════════════════════════════════════════════
  // GENERACIÓN DE SANCIONES
  // ════════════════════════════════════════════════════════════════════════

  /**
   * POST /api/sanciones/prestamo/{idPrestamo}/procesar
   *
   * Procesa un préstamo vencido y genera la sanción correspondiente.
   * Lo llama el módulo de préstamos al registrar una devolución tardía,
   * o puede invocarse manualmente por un ROLE_BIBLIOTECARIO.
   */
  @PostMapping("/prestamo/{idPrestamo}/procesar")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<SancionResponseDTO> procesarDevolucionTardia(
      @PathVariable Long idPrestamo) {
    SancionResponseDTO response = sancionService.procesarDevolucionTardia(idPrestamo);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * POST /api/sanciones/manual
   *
   * Registra una sanción manual por daño o pérdida de un ejemplar.
   * Solo ROLE_BIBLIOTECARIO y ROLE_ADMIN.
   */
  @PostMapping("/manual")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<SancionResponseDTO> registrarSancionManual(
      @Valid @RequestBody SancionManualRequestDTO request,
      @AuthenticationPrincipal UserDetails userDetails) {
    Long bibliotecarioId = extractUserId(userDetails);
    SancionResponseDTO response = sancionService.registrarSancionManual(request, bibliotecarioId);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  // ════════════════════════════════════════════════════════════════════════
  // CONSULTAS
  // ════════════════════════════════════════════════════════════════════════

  /**
   * GET /api/sanciones/{idSancion}
   *
   * Obtiene una sanción por su ID.
   * Un ROLE_ESTUDIANTE solo puede ver sus propias sanciones (verificado en
   * servicio).
   */
  @GetMapping("/{idSancion}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO', 'ROLE_ESTUDIANTE')")
  public ResponseEntity<SancionResponseDTO> buscarPorId(@PathVariable Long idSancion) {
    return ResponseEntity.ok(sancionService.buscarPorId(idSancion));
  }

  /**
   * GET /api/sanciones/usuario/{usuarioId}/historial
   *
   * Historial completo de sanciones de un usuario.
   */
  @GetMapping("/usuario/{usuarioId}/historial")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')" +
      " or (hasRole('ROLE_ESTUDIANTE') and #usuarioId == authentication.principal.id)")
  public ResponseEntity<List<SancionResponseDTO>> historialPorUsuario(
      @PathVariable Long usuarioId) {
    return ResponseEntity.ok(sancionService.historialPorUsuario(usuarioId));
  }

  /**
   * GET /api/sanciones/usuario/{usuarioId}/activas
   *
   * Sanciones activas de un usuario.
   */
  @GetMapping("/usuario/{usuarioId}/activas")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')" +
      " or (hasRole('ROLE_ESTUDIANTE') and #usuarioId == authentication.principal.id)")
  public ResponseEntity<List<SancionResponseDTO>> sancionesActivasPorUsuario(
      @PathVariable Long usuarioId) {
    return ResponseEntity.ok(sancionService.sancionesActivasPorUsuario(usuarioId));
  }

  /**
   * GET /api/sanciones/usuario/{usuarioId}/estado
   *
   * Estado consolidado: tiene suspensión vigente, deuda pendiente, total activas.
   * Endpoint que consultan préstamos y reservas antes de permitir operaciones.
   */
  @GetMapping("/usuario/{usuarioId}/estado")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')" +
      " or (hasRole('ROLE_ESTUDIANTE') and #usuarioId == authentication.principal.id)")
  public ResponseEntity<EstadoSancionUsuarioDTO> obtenerEstadoSanciones(
      @PathVariable Long usuarioId) {
    return ResponseEntity.ok(sancionService.obtenerEstadoSanciones(usuarioId));
  }

  /**
   * GET /api/sanciones/biblioteca/{bibliotecaId}?estado=ACTIVA
   *
   * Lista sanciones de una biblioteca filtradas por estado.
   * Para el panel administrativo del bibliotecario.
   */
  @GetMapping("/biblioteca/{bibliotecaId}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<List<SancionResponseDTO>> sancionesPorBiblioteca(
      @PathVariable Long bibliotecaId,
      @RequestParam(defaultValue = "ACTIVA") EstadoSancion estado) {
    return ResponseEntity.ok(sancionService.sancionesPorBiblioteca(bibliotecaId, estado));
  }

  // ════════════════════════════════════════════════════════════════════════
  // GESTIÓN DE ESTADO
  // ════════════════════════════════════════════════════════════════════════

  /**
   * PATCH /api/sanciones/{idSancion}/pago
   *
   * Registra el pago de una multa. Cambia estado a PAGADA.
   */
  @PatchMapping("/{idSancion}/pago")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<SancionResponseDTO> registrarPago(
      @PathVariable Long idSancion,
      @Valid @RequestBody PagoMultaRequestDTO request) {
    return ResponseEntity.ok(sancionService.registrarPago(idSancion, request));
  }

  /**
   * PATCH /api/sanciones/{idSancion}/condonar
   *
   * Condona una sanción. Solo ROLE_ADMIN puede condonar.
   */
  @PatchMapping("/{idSancion}/condonar")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<SancionResponseDTO> condonar(
      @PathVariable Long idSancion,
      @Valid @RequestBody CondonacionRequestDTO request) {
    return ResponseEntity.ok(sancionService.condonar(idSancion, request));
  }

  private Long extractUserId(UserDetails userDetails) {
    // Adaptación según la implementación de UserDetails del proyecto

    if (userDetails instanceof UserDetailsImpl principal) {
      return principal.getId();
    }
    throw new RuntimeException("No se pudo obtener el ID del usuario autenticado");
  }

}