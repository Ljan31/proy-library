package com.proyecto.fhce.library.controllers;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.MediaType;

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.services.loads.CertificadoNoDeudaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/certificados")
public class CertificadoController {

  @Autowired
  private CertificadoNoDeudaService certificadoService;

  @GetMapping("/usuario/{usuarioId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<ApiResponse<List<CertificadoResponse>>> findByUsuario(@PathVariable Long usuarioId,
      Authentication authentication) {
    Long UserID = obtenerUsuarioId(authentication);
    List<CertificadoResponse> certificados = certificadoService.findByUsuario(usuarioId, UserID,
        authentication.getAuthorities());
    return ResponseEntity.ok(ApiResponse.success(certificados));
  }

  @GetMapping("/validar/{codigo}")
  public ResponseEntity<ApiResponse<ValidacionCertificadoResponse>> validar(@PathVariable String codigo) {
    ValidacionCertificadoResponse validacion = certificadoService.validar(codigo);
    return ResponseEntity.ok(ApiResponse.success(validacion));
  }

  @PostMapping
  @PreAuthorize("hasRole('BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<CertificadoResponse>> generar(
      @Valid @RequestBody CertificadoRequest request,
      Authentication authentication) {

    Long bibliotecarioId = obtenerUsuarioId(authentication);
    CertificadoResponse certificado = certificadoService.generar(request, bibliotecarioId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Certificado generado exitosamente", certificado));
  }

  @GetMapping("/{id}/download")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Resource> download(@PathVariable Long id) throws MalformedURLException {
    CertificadoResponse certificado = certificadoService.findById(id);
    Path path = Paths.get(certificado.getPdf_generado());
    Resource resource = new UrlResource(path.toUri());

    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_PDF)
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + path.getFileName() + "\"")
        .body(resource);
  }

  private Long obtenerUsuarioId(Authentication authentication) {
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    return userDetails.getId();
  }
}
