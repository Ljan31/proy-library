package com.proyecto.fhce.library.controllers;

import com.proyecto.fhce.library.dto.request.library.CancelarReservaRequest;
import com.proyecto.fhce.library.dto.request.library.ConvertirReservaRequest;
import com.proyecto.fhce.library.dto.request.library.ReservaRequest;
import com.proyecto.fhce.library.dto.response.library.ReservaResponse;
import com.proyecto.fhce.library.enums.EstadoReserva;
import com.proyecto.fhce.library.security.UserDetailsImpl;
import com.proyecto.fhce.library.services.library.ReservaService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

  @Autowired
  private ReservaService reservaService;

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO', 'ROLE_ESTUDIANTE')")
  public ResponseEntity<ReservaResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(reservaService.findById(id));
  }

  @GetMapping("/mis-reservas")
  @PreAuthorize("hasAnyRole('ROLE_ESTUDIANTE', 'ROLE_DOCENTE', 'ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<List<ReservaResponse>> getMisReservas(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    return ResponseEntity.ok(reservaService.findByUsuario(userDetails.getId()));
  }

  /**
   * GET /api/reservas/usuario/{usuarioId}
   * Lista reservas de un usuario específico.
   * Solo accesible por admin y bibliotecarios.
   */
  @GetMapping("/usuario/{usuarioId}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<List<ReservaResponse>> getByUsuario(@PathVariable Long usuarioId) {
    return ResponseEntity.ok(reservaService.findByUsuario(usuarioId));
  }

  /**
   * GET /api/reservas/biblioteca/{bibliotecaId}?estado=ACTIVA
   * Lista reservas de una biblioteca, con filtro opcional por estado.
   * Solo accesible por admin y bibliotecarios de la biblioteca.
   */
  @GetMapping("/biblioteca/{bibliotecaId}")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<List<ReservaResponse>> getByBiblioteca(
      @PathVariable Long bibliotecaId,
      @RequestParam(required = false) EstadoReserva estado) {
    return ResponseEntity.ok(reservaService.findByBiblioteca(bibliotecaId, estado));
  }

  /**
   * GET /api/reservas/cola?libroId=1&bibliotecaId=2
   * Muestra la cola de espera de un libro en una biblioteca, ordenada por
   * prioridad.
   * Útil para el panel del bibliotecario.
   */
  @GetMapping("/cola")
  @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<List<ReservaResponse>> getColaEspera(
      @RequestParam Long libroId,
      @RequestParam Long bibliotecaId) {
    return ResponseEntity.ok(reservaService.findColaEspera(libroId, bibliotecaId));
  }

  /**
   * GET /api/reservas/estado/{estado}
   * Lista todas las reservas en un estado dado. Solo para admin.
   */
  @GetMapping("/estado/{estado}")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  public ResponseEntity<List<ReservaResponse>> getByEstado(@PathVariable EstadoReserva estado) {
    return ResponseEntity.ok(reservaService.findByEstado(estado));
  }

  // ==================== ENDPOINTS DE OPERACIÓN ====================

  /**
   * POST /api/reservas
   * Crea una nueva reserva para el usuario autenticado.
   *
   * El userId se extrae del token JWT — nunca del body.
   * Esto garantiza que un usuario solo pueda reservar para sí mismo.
   *
   * Body: { "libroId": 1, "bibliotecaId": 2, "observaciones": "..." }
   * Response 201 Created con la reserva creada.
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('ROLE_ESTUDIANTE', 'ROLE_DOCENTE', 'ROLE_ADMIN')")
  public ResponseEntity<ReservaResponse> crear(
      @Valid @RequestBody ReservaRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    ReservaResponse response = reservaService.crearReserva(request, userDetails.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /**
   * PATCH /api/reservas/{id}/cancelar
   * Cancela una reserva ACTIVA o NOTIFICADA.
   *
   * El servicio verifica que el usuario autenticado sea el dueño
   * o un bibliotecario autorizado de esa biblioteca.
   *
   * Body: { "motivo": "..." } (opcional)
   * Response 200 OK con la reserva actualizada.
   */
  @PatchMapping("/{id}/cancelar")
  @PreAuthorize("hasAnyRole('ROLE_ESTUDIANTE', 'ROLE_DOCENTE', 'ROLE_ADMIN', 'ROLE_BIBLIOTECARIO')")
  public ResponseEntity<ReservaResponse> cancelar(
      @PathVariable Long id,
      @RequestBody(required = false) CancelarReservaRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {

    boolean esBibliotecario = userDetails.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_BIBLIOTECARIO"));

    ReservaResponse response = reservaService.cancelarReserva(
        id, userDetails.getId(), esBibliotecario, request);
    return ResponseEntity.ok(response);
  }

  /**
   * POST /api/reservas/convertir
   * Convierte una reserva NOTIFICADA en préstamo cuando el usuario retira el
   * libro.
   * Solo accesible por bibliotecarios y admin.
   *
   * Body: { "reservaId": 1, "condicionEntrega": "BUENO", "tipoDocumentoGarantia":
   * "CI" }
   * Response 200 OK con la reserva actualizada (estado ATENDIDA + prestamoId).
   */
  @PostMapping("/convertir")
  @PreAuthorize("hasAnyRole('ROLE_BIBLIOTECARIO', 'ROLE_ADMIN')")
  public ResponseEntity<ReservaResponse> convertirEnPrestamo(
      @Valid @RequestBody ConvertirReservaRequest request,
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    ReservaResponse response = reservaService.convertirEnPrestamo(request, userDetails.getId());
    return ResponseEntity.ok(response);
  }
}