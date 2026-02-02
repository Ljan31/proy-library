package com.proyecto.fhce.library.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.dto.request.PermisoRequest;
import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.dto.response.PermisoResponse;
import com.proyecto.fhce.library.services.PermisoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/permisos")
public class PermisoController {
  @Autowired
  private PermisoService permisoService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<PermisoResponse>>> findAll() {
    List<PermisoResponse> permisos = permisoService.findAll();
    return ResponseEntity.ok(ApiResponse.success(permisos));
  }

  @GetMapping("/grouped-by-modulo")
  public ResponseEntity<ApiResponse<Map<String, List<PermisoResponse>>>> findAllGroupedByModulo() {
    Map<String, List<PermisoResponse>> permisosAgrupados = permisoService.findAllGroupedByModulo();
    return ResponseEntity.ok(ApiResponse.success(permisosAgrupados));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PermisoResponse>> findById(@PathVariable Long id) {
    PermisoResponse permiso = permisoService.findById(id);
    return ResponseEntity.ok(ApiResponse.success(permiso));
  }

  @GetMapping("/nombre/{nombre}")
  public ResponseEntity<ApiResponse<PermisoResponse>> findByNombre(@PathVariable String nombre) {
    PermisoResponse permiso = permisoService.findByNombre(nombre);
    return ResponseEntity.ok(ApiResponse.success(permiso));
  }

  @GetMapping("/modulo/{modulo}")
  public ResponseEntity<ApiResponse<List<PermisoResponse>>> findByModulo(@PathVariable String modulo) {
    List<PermisoResponse> permisos = permisoService.findByModulo(modulo);
    return ResponseEntity.ok(ApiResponse.success(permisos));
  }

  @GetMapping("/modulos")
  public ResponseEntity<ApiResponse<List<String>>> findModulos() {
    List<String> modulos = permisoService.findModulos();
    return ResponseEntity.ok(ApiResponse.success(modulos));
  }

  @GetMapping("/role/{roleId}")
  public ResponseEntity<ApiResponse<List<PermisoResponse>>> findByRoleId(@PathVariable Long roleId) {
    List<PermisoResponse> permisos = permisoService.findByRoleId(roleId);
    return ResponseEntity.ok(ApiResponse.success(permisos));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PermisoResponse>> create(@Valid @RequestBody PermisoRequest request) {
    PermisoResponse permiso = permisoService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Permiso creado exitosamente", permiso));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PermisoResponse>> update(
      @PathVariable Long id,
      @Valid @RequestBody PermisoRequest request) {
    PermisoResponse permiso = permisoService.update(id, request);
    return ResponseEntity.ok(ApiResponse.success("Permiso actualizado exitosamente", permiso));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    permisoService.delete(id);
    return ResponseEntity.ok(ApiResponse.success("Permiso eliminado exitosamente", null));
  }
}
