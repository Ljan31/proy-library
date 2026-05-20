package com.proyecto.fhce.library.controllers;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import com.proyecto.fhce.library.dto.request.library.SolicitudCertificadoRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.library.SolicitudCertificadoResponse;
import com.proyecto.fhce.library.enums.EstadoSolicitud;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.services.library.SolicitudCertificadoService;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes-certificado")
public class SolicitudCertificadoController {

  @Autowired
  private SolicitudCertificadoService solicitudService;

  /**
   * Crear solicitud de certificado.
   *
   * Puede hacerlo:
   * - ESTUDIANTE
   * - BIBLIOTECARIO
   * - ADMIN
   *
   * Incluso si el usuario no existe,
   * la solicitud queda registrada.
   */
  @PostMapping
  @PreAuthorize("permitAll()")
  public ResponseEntity<ApiResponse<SolicitudCertificadoResponse>> crearSolicitud(
      @Valid @RequestBody SolicitudCertificadoRequest request,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);

    SolicitudCertificadoResponse response = solicitudService.crearSolicitud(
        request,
        solicitanteId,
        authentication != null
            ? authentication.getAuthorities()
            : null);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Solicitud enviada correctamente",
                response));
  }

  /**
   * ADMIN/BIBLIOTECARIO:
   * Ver solicitudes de una biblioteca.
   */
  @GetMapping("/biblioteca/{bibliotecaId}")
  @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<SolicitudCertificadoResponse>>> listarPorBiblioteca(
      @PathVariable Long bibliotecaId,
      @RequestParam(required = false) EstadoSolicitud estado,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);

    List<SolicitudCertificadoResponse> response = solicitudService.listarPorBiblioteca(
        bibliotecaId,
        estado,
        solicitanteId,
        authentication.getAuthorities());

    return ResponseEntity.ok(
        ApiResponse.success(response));
  }

  /**
   * Ver detalle de solicitud.
   */
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<SolicitudCertificadoResponse>> obtenerPorId(
      @PathVariable Long id,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);

    SolicitudCertificadoResponse response = solicitudService.obtenerPorId(
        id,
        solicitanteId,
        authentication.getAuthorities());

    return ResponseEntity.ok(
        ApiResponse.success(response));
  }

  /**
   * Aprobar solicitud.
   *
   * Solo ADMIN o BIBLIOTECARIO.
   */
  @PatchMapping("/{id}/aprobar")
  @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<SolicitudCertificadoResponse>> aprobar(
      @PathVariable Long id,
      @RequestParam(required = false) String observacion,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);

    SolicitudCertificadoResponse response = solicitudService.aprobarSolicitud(
        id,
        observacion,
        solicitanteId,
        authentication.getAuthorities());

    return ResponseEntity.ok(
        ApiResponse.success(
            "Solicitud aprobada",
            response));
  }

  /**
   * Rechazar solicitud.
   */
  @PatchMapping("/{id}/rechazar")
  @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<SolicitudCertificadoResponse>> rechazar(
      @PathVariable Long id,
      @RequestParam(required = false) String observacion,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);

    SolicitudCertificadoResponse response = solicitudService.rechazarSolicitud(
        id,
        observacion,
        solicitanteId,
        authentication.getAuthorities());

    return ResponseEntity.ok(
        ApiResponse.success(
            "Solicitud rechazada",
            response));
  }

  // =====================================================
  // HELPERS
  // =====================================================

  private Long obtenerUsuarioId(
      Authentication authentication) {

    if (authentication == null ||
        !(authentication.getPrincipal() instanceof UserDetailsImpl)) {

      return null;
    }

    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    return userDetails.getId();
  }
}