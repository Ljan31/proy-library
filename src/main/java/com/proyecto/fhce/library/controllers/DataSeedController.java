package com.proyecto.fhce.library.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.proyecto.fhce.library.dto.response.ApiResponse;
import com.proyecto.fhce.library.services.DataSeedService;

@Profile({ "dev", "test" })
@RestController
@RequestMapping("/api/seed")
public class DataSeedController {

  @Autowired
  private DataSeedService dataSeedService;

  // ⚠️ Este endpoint solo para desarrollo/test
  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<String>> runSeed() {
    try {
      String data = dataSeedService.seed(); // Ejecuta la carga de datos inicial
      return ResponseEntity.ok(ApiResponse.success("Seed ejecutado correctamente", data));
    } catch (Exception e) {
      // Manejo genérico de errores para que devuelva JSON
      return ResponseEntity.internalServerError()
          .body(ApiResponse.error("Error al ejecutar seed: " + e.getMessage()));
    }
  }

  @GetMapping
  public ResponseEntity<?> test() {
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .body(ApiResponse.success("Hola JSON"));
  }
}