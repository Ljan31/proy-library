package com.proyecto.fhce.library.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;
import com.proyecto.fhce.library.enums.EstadoCertificado;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.services.loads.CertificadoNoDeudaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

  @Autowired
  private CertificadoNoDeudaService certificadoService;

  @Value("${app.certificados.ruta-base}")
  private String rutaBase;

  /**
   * ADMIN: genera certificado para cualquier usuario en cualquier biblioteca.
   * BIBLIOTECARIO: solo puede generar para su biblioteca asignada.
   * ESTUDIANTE: solo puede generar el suyo propio, eligiendo su biblioteca.
   *
   * POST /api/certificados
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<CertificadoResponse>> generar(
      @Valid @RequestBody CertificadoRequest request,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);
    CertificadoResponse certificado = certificadoService.generar(request, solicitanteId,
        authentication.getAuthorities());

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Certificado generado exitosamente", certificado));
  }

  /**
   * Consulta certificados de un usuario.
   * ADMIN/BIBLIOTECARIO: puede ver de cualquier usuario.
   * ESTUDIANTE: solo los suyos propios.
   *
   * GET /api/certificados/usuario/{usuarioId}
   * GET /api/certificados/usuario/{usuarioId}?bibliotecaId=3 (filtro opcional)
   */
  @GetMapping("/usuario/{usuarioId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<CertificadoResponse>>> findByUsuario(
      @PathVariable Long usuarioId,
      @RequestParam(required = false) Long bibliotecaId,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);
    List<CertificadoResponse> certificados = certificadoService
        .findByUsuario(usuarioId, solicitanteId, bibliotecaId, authentication.getAuthorities());

    return ResponseEntity.ok(ApiResponse.success(certificados));
  }

  @GetMapping("/usuarioCi")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<CertificadoResponse>>> buscarPorCi(
      @RequestParam String ci,
      @RequestParam Long bibliotecaId) {

    List<CertificadoResponse> certificados = certificadoService.findByCiAndBiblioteca(ci, bibliotecaId);

    return ResponseEntity.ok(ApiResponse.success(certificados));
  }

  /**
   * BIBLIOTECARIO/ADMIN: todos los certificados emitidos en una biblioteca.
   *
   * GET /api/certificados/biblioteca/{bibliotecaId}
   * GET /api/certificados/biblioteca/{bibliotecaId}?estado=VIGENTE
   */
  @GetMapping("/biblioteca/{bibliotecaId}")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<List<CertificadoResponse>>> findByBiblioteca(
      @PathVariable Long bibliotecaId,
      @RequestParam(required = false) EstadoCertificado estado,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);
    List<CertificadoResponse> certificados = certificadoService
        .findByBiblioteca(bibliotecaId, estado, solicitanteId, authentication.getAuthorities());

    return ResponseEntity.ok(ApiResponse.success(certificados));
  }

  /**
   * Validación pública del certificado por código (QR, link externo, etc.).
   * No requiere autenticación — es para verificación externa.
   *
   * GET /api/certificados/validar/{codigo}
   */
  @GetMapping("/validar/{codigo}")
  public ResponseEntity<ApiResponse<ValidacionCertificadoResponse>> validar(
      @PathVariable String codigo) {

    ValidacionCertificadoResponse validacion = certificadoService.validar(codigo);
    return ResponseEntity.ok(ApiResponse.success(validacion));
  }

  /**
   * Descarga del PDF del certificado.
   * Solo el propietario, su bibliotecario, o admin.
   *
   * GET /api/certificados/{id}/download
   */
  @GetMapping("/{id}/download")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<byte[]> download(
      @PathVariable Long id,
      Authentication authentication) throws Exception, IOException {

    Long solicitanteId = obtenerUsuarioId(authentication);
    byte[] archivo = certificadoService
        .generarCertificadoPDF(id, solicitanteId, authentication.getAuthorities());

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"certificado-" + id + ".pdf\"")
        .body(archivo);
  }

  /**
   * ADMIN/BIBLIOTECARIO: anular un certificado vigente manualmente.
   *
   * PATCH /api/certificados/{id}/anular
   */
  @PatchMapping("/{id}/anular")
  @PreAuthorize("hasAnyRole('BIBLIOTECARIO', 'ADMIN')")
  public ResponseEntity<ApiResponse<CertificadoResponse>> anular(
      @PathVariable Long id,
      Authentication authentication) {

    Long solicitanteId = obtenerUsuarioId(authentication);
    CertificadoResponse certificado = certificadoService
        .anular(id, solicitanteId, authentication.getAuthorities());

    return ResponseEntity.ok(ApiResponse.success("Certificado anulado", certificado));
  }

  private Long obtenerUsuarioId(Authentication authentication) {
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    return userDetails.getId();
  }
}
