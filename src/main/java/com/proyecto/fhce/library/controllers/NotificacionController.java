package com.proyecto.fhce.library.controllers;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.proyecto.fhce.library.dto.request.CrearNotificacionRequest;
import com.proyecto.fhce.library.dto.response.ContadorNoLeidasResponse;
import com.proyecto.fhce.library.dto.response.NotificacionResponse;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.services.notificaciones.NotificacionService;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

  private final NotificacionService notificacionService;

  public NotificacionController(NotificacionService notificacionService) {
    this.notificacionService = notificacionService;
  }

  /** GET /api/v1/notificaciones — bandeja paginada del usuario autenticado */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Page<NotificacionResponse>> listarMisNotificaciones(
      @AuthenticationPrincipal UserDetails userDetails,
      @PageableDefault(size = 20, sort = "fechaCreacion") Pageable pageable) {
    Long idUsuario = resolverIdUsuario(userDetails);
    return ResponseEntity.ok(notificacionService.listarPorUsuario(idUsuario, pageable));
  }

  /** GET /api/v1/notificaciones/no-leidas */
  @GetMapping("/no-leidas")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<NotificacionResponse>> listarNoLeidas(
      @AuthenticationPrincipal UserDetails userDetails) {
    Long idUsuario = resolverIdUsuario(userDetails);
    return ResponseEntity.ok(notificacionService.listarNoLeidas(idUsuario));
  }

  /** GET /api/v1/notificaciones/no-leidas/contador — badge del menú */
  @GetMapping("/no-leidas/contador")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ContadorNoLeidasResponse> contarNoLeidas(
      @AuthenticationPrincipal UserDetails userDetails) {
    Long idUsuario = resolverIdUsuario(userDetails);
    return ResponseEntity.ok(notificacionService.contarNoLeidas(idUsuario));
  }

  /** PATCH /api/v1/notificaciones/{id}/leida — IDOR check en el servicio */
  @PatchMapping("/{id}/leida")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<NotificacionResponse> marcarComoLeida(
      @PathVariable Long id,
      @AuthenticationPrincipal UserDetails userDetails) {
    Long idUsuario = resolverIdUsuario(userDetails);
    return ResponseEntity.ok(notificacionService.marcarComoLeida(id, idUsuario));
  }

  /** PATCH /api/v1/notificaciones/leidas — marca todas como leídas */
  @PatchMapping("/leidas")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> marcarTodasComoLeidas(
      @AuthenticationPrincipal UserDetails userDetails) {
    Long idUsuario = resolverIdUsuario(userDetails);
    notificacionService.marcarTodasComoLeidas(idUsuario);
    return ResponseEntity.noContent().build();
  }

  /** POST /api/v1/notificaciones — solo ADMIN o BIBLIOTECARIO */
  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
  public ResponseEntity<NotificacionResponse> crear(
      @Valid @RequestBody CrearNotificacionRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(notificacionService.crear(request));
  }

  // -------------------------------------------------------
  // Utilidad privada
  // -------------------------------------------------------

  /**
   * Extrae el idUsuario del token JWT.
   * TODO: adaptar según la implementación de UserDetails del proyecto SIGEB.
   * Opción A: ((UsuarioPrincipal) userDetails).getIdUsuario()
   * Opción B: Long.parseLong(userDetails.getUsername()) — si username es el ID
   */
  private Long resolverIdUsuario(UserDetails userDetails) {
    if (userDetails instanceof UserDetailsImpl userDetailsImpl) {
      return userDetailsImpl.getId();
    }

    throw new IllegalArgumentException(
        "Tipo de UserDetails no soportado: " + userDetails.getClass().getName());
  }
}