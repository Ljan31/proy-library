package com.proyecto.fhce.library.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.loads.DevolucionRequest;
import com.proyecto.fhce.library.dto.request.loads.FiltroPrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.PrestamoRequest;
import com.proyecto.fhce.library.dto.request.loads.RenovacionRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.loads.PrestamoResponse;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.services.loads.PrestamoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

  @Autowired
  private PrestamoService prestamoService;

  // ==================== GET endpoints ====================

  @GetMapping
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<PrestamoResponse>>> findAll() {
    List<PrestamoResponse> prestamos = prestamoService.findAll();
    return ResponseEntity.ok(ApiResponse.success(prestamos));
  }

  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PrestamoResponse>> findById(@PathVariable Long id) {
    PrestamoResponse prestamo = prestamoService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(prestamo));
  }

  @GetMapping("/usuario/{usuarioId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<PrestamoResponse>>> findByUsuario(@PathVariable Long usuarioId) {
    List<PrestamoResponse> prestamos = prestamoService.findByUsuario(usuarioId);
    return ResponseEntity.ok(ApiResponse.success(prestamos));
  }

  /**
   * Returns active loans for the currently authenticated user.
   * Useful for user self-service views.
   */
  @GetMapping("/mis-prestamos")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<PrestamoResponse>>> findMisPrestamos(
      Authentication authentication) {
    Long usuarioId = obtenerUsuarioId(authentication);
    List<PrestamoResponse> prestamos = prestamoService.findByUsuario(usuarioId);
    return ResponseEntity.ok(ApiResponse.success(prestamos));
  }

  @GetMapping("/estado/{estado}")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<PrestamoResponse>>> findByEstado(
      @PathVariable EstadoPrestamo estado) {
    List<PrestamoResponse> prestamos = prestamoService.findByEstado(estado);
    return ResponseEntity.ok(ApiResponse.success(prestamos));
  }

  @GetMapping("/vencidos")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<PrestamoResponse>>> findVencidos() {
    List<PrestamoResponse> prestamos = prestamoService.findPrestamosVencidos();
    return ResponseEntity.ok(ApiResponse.success(prestamos));
  }

  @GetMapping("/por-vencer")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<PrestamoResponse>>> findPorVencer(
      @RequestParam(defaultValue = "3") int dias) {
    List<PrestamoResponse> prestamos = prestamoService.findPrestamosPorVencer(dias);
    return ResponseEntity.ok(ApiResponse.success(prestamos));
  }

  // ==================== POST endpoints ====================

  @PostMapping("/filtrar")
  @PreAuthorize("hasRole('ADMIN') or hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<PrestamoResponse>>> filtrar(
      @RequestBody FiltroPrestamoRequest filtro) {
    List<PrestamoResponse> prestamos = prestamoService.filtrar(filtro);
    return ResponseEntity.ok(ApiResponse.success(prestamos));
  }

  @PostMapping
  @PreAuthorize("hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<PrestamoResponse>> realizarPrestamo(
      @Valid @RequestBody PrestamoRequest request,
      Authentication authentication) {
    Long bibliotecarioId = obtenerUsuarioId(authentication);
    PrestamoResponse prestamo = prestamoService.realizarPrestamo(request, bibliotecarioId);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Préstamo realizado exitosamente", prestamo));
  }

  @PostMapping("/devolucion")
  @PreAuthorize("hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<PrestamoResponse>> realizarDevolucion(
      @Valid @RequestBody DevolucionRequest request,
      Authentication authentication) {
    Long bibliotecarioId = obtenerUsuarioId(authentication);
    PrestamoResponse prestamo = prestamoService.realizarDevolucion(request, bibliotecarioId);
    return ResponseEntity.ok(ApiResponse.success("Devolución registrada exitosamente", prestamo));
  }

  @PostMapping("/renovar")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<PrestamoResponse>> renovar(
      @Valid @RequestBody RenovacionRequest request, Authentication authentication) {
    Long userID = obtenerUsuarioId(authentication);
    PrestamoResponse prestamo = prestamoService.renovarPrestamo(request, userID, authentication.getAuthorities());
    return ResponseEntity.ok(ApiResponse.success("Préstamo renovado exitosamente", prestamo));
  }

  // ==================== Private helpers ====================

  private Long obtenerUsuarioId(Authentication authentication) {
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    return userDetails.getId();
  }
}