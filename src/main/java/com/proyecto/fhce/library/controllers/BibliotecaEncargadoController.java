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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.AsignarEncargadoRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.BibliotecaEncargadoResponse;
import com.proyecto.fhce.library.services.BibliotecaEncargadoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bibliotecas/{bibliotecaId}/encargados")
@Tag(name = "Encargados de Biblioteca", description = "Gestión de encargados por biblioteca")
public class BibliotecaEncargadoController {

  @Autowired
  private BibliotecaEncargadoService encargadoService;

  @Operation(summary = "Listar encargados activos de una biblioteca")
  @GetMapping
  @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
  public ResponseEntity<ApiResponse<List<BibliotecaEncargadoResponse>>> findEncargados(
      @PathVariable Long bibliotecaId) {
    return ResponseEntity.ok(
        ApiResponse.success(encargadoService.findEncargadosByBiblioteca(bibliotecaId)));
  }

  @Operation(summary = "Asignar encargado a una biblioteca", security = @SecurityRequirement(name = "bearer-jwt"))
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<BibliotecaEncargadoResponse>> asignar(
      @PathVariable Long bibliotecaId,
      @Valid @RequestBody AsignarEncargadoRequest request) {
    BibliotecaEncargadoResponse response = encargadoService.asignarEncargado(bibliotecaId, request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Encargado asignado exitosamente", response));
  }

  @Operation(summary = "Remover encargado de una biblioteca", security = @SecurityRequirement(name = "bearer-jwt"))
  @DeleteMapping("/{usuarioId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<Void>> remover(
      @PathVariable Long bibliotecaId,
      @PathVariable Long usuarioId) {
    System.out.println("====================desde encargado delete");
    encargadoService.removerEncargado(bibliotecaId, usuarioId);
    return ResponseEntity.ok(ApiResponse.success("Encargado removido exitosamente", null));
  }
}