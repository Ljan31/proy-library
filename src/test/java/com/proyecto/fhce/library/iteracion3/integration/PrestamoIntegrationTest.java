package com.proyecto.fhce.library.iteracion3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.proyecto.fhce.library.entities.Ejemplar;
import com.proyecto.fhce.library.entities.Prestamo;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.EstadoPrestamo;
import com.proyecto.fhce.library.services.loads.PrestamoService;

/**
 * Iteración 3 · Integración de préstamos, devoluciones y renovaciones:
 * API real + seguridad JWT por rol + JPA sobre H2.
 */
@DisplayName("Iteración 3 · Préstamos, devoluciones y renovaciones (integración)")
class PrestamoIntegrationTest extends PrestamoIntegrationBase {

  @Autowired
  PrestamoService prestamoService;

  private MvcResult postDevolucion(String token, Map<String, Object> body) throws Exception {
    return mockMvc.perform(post("/api/prestamos/devolucion").header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON).content(json(body))).andReturn();
  }

  private Map<String, Object> cuerpoDevolucion(long idPrestamo, String condicion) {
    Map<String, Object> m = new HashMap<>();
    m.put("prestamoId", idPrestamo);
    m.put("condicionDevolucion", condicion);
    return m;
  }

  private MvcResult postRenovar(String token, long idPrestamo) throws Exception {
    return mockMvc.perform(post("/api/prestamos/renovar").header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("prestamoId", idPrestamo, "motivo", "Necesito más tiempo")))).andReturn();
  }

  private EstadoEjemplar estadoEjemplar(long idEjemplar) {
    em.flush();
    em.clear();
    Ejemplar e = ejemplarRepository.findById(idEjemplar).orElseThrow();
    return e.getEstadoEjemplar();
  }

  // ===================== REALIZAR PRÉSTAMO =====================

  @Test
  @DisplayName("IT3-I-PRE-01: el bibliotecario registra un préstamo; queda ACTIVO y el ejemplar pasa a PRESTADO")
  void prestamo_ok() throws Exception {
    long ejemplar = crearEjemplar();

    mockMvc.perform(post("/api/prestamos").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(cuerpoPrestamo(estudiante1.getId_usuario(), ejemplar, "DOMICILIO"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.estadoPrestamo").value("ACTIVO"))
        .andExpect(jsonPath("$.data.renovaciones").value(0))
        .andExpect(jsonPath("$.data.tipoPrestamo").value("DOMICILIO"));

    assertThat(estadoEjemplar(ejemplar)).isEqualTo(EstadoEjemplar.PRESTADO);
  }

  @Test
  @DisplayName("IT3-I-PRE-02: la fecha de devolución por defecto es hoy + días máximos de la configuración (7)")
  void prestamo_fechaPorDefecto() throws Exception {
    long id = crearPrestamoNuevo(estudiante1);

    Prestamo p = recargarPrestamo(id);
    assertThat(p.getFechaDevolucionEstimada()).isEqualTo(java.time.LocalDate.now().plusDays(7));
    assertThat(p.getIdConfigUsado()).isEqualTo(idConfig);
  }

  @Test
  @DisplayName("IT3-I-PRE-03: un ESTUDIANTE no puede registrar préstamos (403)")
  void prestamo_estudianteProhibido() throws Exception {
    long ejemplar = crearEjemplar();

    mockMvc.perform(post("/api/prestamos").header("Authorization", tokenEstudiante1)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(cuerpoPrestamo(estudiante1.getId_usuario(), ejemplar, "DOMICILIO"))))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT3-I-PRE-04: sin token no se puede registrar un préstamo")
  void prestamo_sinToken() throws Exception {
    long ejemplar = crearEjemplar();

    mockMvc.perform(post("/api/prestamos").contentType(MediaType.APPLICATION_JSON)
        .content(json(cuerpoPrestamo(estudiante1.getId_usuario(), ejemplar, "DOMICILIO"))))
        .andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("IT3-I-PRE-05: faltan datos obligatorios (tipo de préstamo) -> 400")
  void prestamo_validacion() throws Exception {
    long ejemplar = crearEjemplar();
    Map<String, Object> body = cuerpoPrestamo(estudiante1.getId_usuario(), ejemplar, "DOMICILIO");
    body.remove("tipoPrestamo");

    mockMvc.perform(post("/api/prestamos").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT3-I-PRE-06: una fecha de devolución en el pasado es rechazada (400)")
  void prestamo_fechaPasada() throws Exception {
    long ejemplar = crearEjemplar();
    Map<String, Object> body = cuerpoPrestamo(estudiante1.getId_usuario(), ejemplar, "DOMICILIO");
    body.put("fechaDevolucionEstimada", java.time.LocalDate.now().minusDays(1).toString());

    mockMvc.perform(post("/api/prestamos").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT3-I-PRE-07: un ejemplar ya prestado no puede prestarse otra vez (409)")
  void prestamo_ejemplarYaPrestado() throws Exception {
    long ejemplar = crearEjemplar();
    crearPrestamo(estudiante1, ejemplar);

    MvcResult r = postPrestamo(tokenBiblio, cuerpoPrestamo(estudiante2.getId_usuario(), ejemplar, "DOMICILIO"));

    assertThat(r.getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-PRE-08: al llegar al límite de ejemplares a domicilio (2) se rechaza el tercero (409)")
  void prestamo_limiteDomicilio() throws Exception {
    crearPrestamoNuevo(estudiante1);
    crearPrestamoNuevo(estudiante1);
    long tercero = crearEjemplar();

    MvcResult r = postPrestamo(tokenBiblio, cuerpoPrestamo(estudiante1.getId_usuario(), tercero, "DOMICILIO"));

    assertThat(r.getResponse().getStatus()).isEqualTo(409);
    assertThat(r.getResponse().getContentAsString()).contains("límite");
    assertThat(estadoEjemplar(tercero)).isEqualTo(EstadoEjemplar.DISPONIBLE);
  }

  @Test
  @DisplayName("IT3-I-PRE-09: un bibliotecario que no es encargado de la biblioteca no puede prestar (409)")
  void prestamo_bibliotecarioNoEncargado() throws Exception {
    long ejemplar = crearEjemplar();

    MvcResult r = postPrestamo(tokenOtroBiblio,
        cuerpoPrestamo(estudiante1.getId_usuario(), ejemplar, "DOMICILIO"));

    assertThat(r.getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-PRE-10: usuario o ejemplar inexistente -> 404")
  void prestamo_inexistentes() throws Exception {
    long ejemplar = crearEjemplar();

    assertThat(postPrestamo(tokenBiblio, cuerpoPrestamo(999999L, ejemplar, "DOMICILIO"))
        .getResponse().getStatus()).isEqualTo(404);
    assertThat(postPrestamo(tokenBiblio, cuerpoPrestamo(estudiante1.getId_usuario(), 999999L, "DOMICILIO"))
        .getResponse().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("IT3-I-PRE-11: los préstamos en SALA usan su propio límite (3) y no el de domicilio (2)")
  void prestamo_limiteSalaDistinto() throws Exception {
    for (int i = 0; i < 3; i++) {
      MvcResult r = postPrestamo(tokenBiblio,
          cuerpoPrestamo(estudiante1.getId_usuario(), crearEjemplar(), "SALA"));
      assertThat(r.getResponse().getStatus()).as("préstamo en sala #" + (i + 1)).isEqualTo(201);
    }
  }

  // ===================== DEVOLUCIÓN =====================

  @Test
  @DisplayName("IT3-I-DEV-01: la devolución deja el préstamo DEVUELTO y el ejemplar DISPONIBLE")
  void devolucion_ok() throws Exception {
    long ejemplar = crearEjemplar();
    long prestamo = crearPrestamo(estudiante1, ejemplar);

    MvcResult r = postDevolucion(tokenBiblio, cuerpoDevolucion(prestamo, "BUENO"));

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(cuerpo(r).path("data").path("estadoPrestamo").asText()).isEqualTo("DEVUELTO");
    Prestamo p = recargarPrestamo(prestamo);
    assertThat(p.getFechaDevolucionReal()).isNotNull();
    assertThat(estadoEjemplar(ejemplar)).isEqualTo(EstadoEjemplar.DISPONIBLE);
  }

  @Test
  @DisplayName("IT3-I-DEV-02: un ejemplar devuelto puede prestarse de nuevo")
  void devolucion_luegoSePuedePrestar() throws Exception {
    long ejemplar = crearEjemplar();
    long prestamo = crearPrestamo(estudiante1, ejemplar);
    postDevolucion(tokenBiblio, cuerpoDevolucion(prestamo, "BUENO"));

    MvcResult r = postPrestamo(tokenBiblio, cuerpoPrestamo(estudiante2.getId_usuario(), ejemplar, "DOMICILIO"));

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
  }

  @Test
  @DisplayName("IT3-I-DEV-03: devolver dos veces el mismo préstamo -> 409")
  void devolucion_doble() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    postDevolucion(tokenBiblio, cuerpoDevolucion(prestamo, "BUENO"));

    assertThat(postDevolucion(tokenBiblio, cuerpoDevolucion(prestamo, "BUENO")).getResponse().getStatus())
        .isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-DEV-04: la condición de devolución es obligatoria (400)")
  void devolucion_sinCondicion() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    Map<String, Object> body = new HashMap<>();
    body.put("prestamoId", prestamo);

    assertThat(postDevolucion(tokenBiblio, body).getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("IT3-I-DEV-05: un ESTUDIANTE no puede registrar devoluciones (403)")
  void devolucion_estudianteProhibido() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);

    assertThat(postDevolucion(tokenEstudiante1, cuerpoDevolucion(prestamo, "BUENO")).getResponse().getStatus())
        .isEqualTo(403);
  }

  @Test
  @DisplayName("IT3-I-DEV-06: devolver con peor condición agrega una alerta en las observaciones")
  void devolucion_deterioro() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);

    postDevolucion(tokenBiblio, cuerpoDevolucion(prestamo, "DETERIORADO"));

    assertThat(recargarPrestamo(prestamo).getObservaciones()).contains("[ALERTA]");
  }

  @Test
  @DisplayName("IT3-I-DEV-07: devolver un préstamo inexistente -> 404")
  void devolucion_inexistente() throws Exception {
    assertThat(postDevolucion(tokenBiblio, cuerpoDevolucion(999999L, "BUENO")).getResponse().getStatus())
        .isEqualTo(404);
  }

  @Test
  @DisplayName("IT3-I-DEV-08: un préstamo VENCIDO también puede devolverse")
  void devolucion_vencido() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    forzarFechaEstimada(prestamo, 3);
    forzarEstado(prestamo, EstadoPrestamo.VENCIDO);

    MvcResult r = postDevolucion(tokenBiblio, cuerpoDevolucion(prestamo, "BUENO"));

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
  }

  // ===================== RENOVACIÓN =====================

  @Test
  @DisplayName("IT3-I-REN-01: el dueño del préstamo lo renueva: RENOVADO, +1 renovación y +7 días")
  void renovar_propietario() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    java.time.LocalDate antes = recargarPrestamo(prestamo).getFechaDevolucionEstimada();

    MvcResult r = postRenovar(tokenEstudiante1, prestamo);

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    Prestamo p = recargarPrestamo(prestamo);
    assertThat(p.getEstadoPrestamo()).isEqualTo(EstadoPrestamo.RENOVADO);
    assertThat(p.getRenovaciones()).isEqualTo(1);
    assertThat(p.getFechaDevolucionEstimada()).isEqualTo(antes.plusDays(7));
  }

  @Test
  @DisplayName("IT3-I-REN-02: otro estudiante no puede renovar un préstamo ajeno (409)")
  void renovar_terceroNoAutorizado() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);

    assertThat(postRenovar(tokenEstudiante2, prestamo).getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-REN-03: el bibliotecario encargado puede renovar el préstamo de un estudiante")
  void renovar_bibliotecario() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);

    assertThat(postRenovar(tokenBiblio, prestamo).getResponse().getStatus()).isEqualTo(200);
  }

  @Test
  @DisplayName("IT3-I-REN-04: tras agotar las renovaciones (2) la tercera es rechazada (409)")
  void renovar_limite() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    assertThat(postRenovar(tokenEstudiante1, prestamo).getResponse().getStatus()).isEqualTo(200);
    assertThat(postRenovar(tokenEstudiante1, prestamo).getResponse().getStatus()).isEqualTo(200);

    MvcResult r = postRenovar(tokenEstudiante1, prestamo);

    assertThat(r.getResponse().getStatus()).isEqualTo(409);
    assertThat(recargarPrestamo(prestamo).getRenovaciones()).isEqualTo(2);
  }

  @Test
  @DisplayName("IT3-I-REN-05: no se puede renovar un préstamo cuya fecha ya pasó (409)")
  void renovar_fechaVencida() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    forzarFechaEstimada(prestamo, 2);

    assertThat(postRenovar(tokenEstudiante1, prestamo).getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-REN-06: no se puede renovar un préstamo ya devuelto (409)")
  void renovar_yaDevuelto() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    postDevolucion(tokenBiblio, cuerpoDevolucion(prestamo, "BUENO"));

    assertThat(postRenovar(tokenEstudiante1, prestamo).getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-REN-07: renovar sin autenticación es rechazado")
  void renovar_sinToken() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);

    mockMvc.perform(post("/api/prestamos/renovar").contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("prestamoId", prestamo)))).andExpect(status().is4xxClientError());
  }

  // ===================== CONSULTAS =====================

  @Test
  @DisplayName("IT3-I-QRY-01: 'mis préstamos' devuelve solo los del usuario autenticado")
  void consulta_misPrestamos() throws Exception {
    crearPrestamoNuevo(estudiante1);
    crearPrestamoNuevo(estudiante1);
    crearPrestamoNuevo(estudiante2);

    mockMvc.perform(get("/api/prestamos/mis-prestamos").header("Authorization", tokenEstudiante1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  @DisplayName("IT3-I-QRY-02: el listado general es solo para personal (estudiante 403, bibliotecario 200)")
  void consulta_listadoGeneral() throws Exception {
    crearPrestamoNuevo(estudiante1);

    mockMvc.perform(get("/api/prestamos").header("Authorization", tokenEstudiante1))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/prestamos").header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("IT3-I-QRY-03: consultar por biblioteca con filtro de estado")
  void consulta_porBibliotecaYEstado() throws Exception {
    long p1 = crearPrestamoNuevo(estudiante1);
    crearPrestamoNuevo(estudiante2);
    postDevolucion(tokenBiblio, cuerpoDevolucion(p1, "BUENO"));

    mockMvc.perform(get("/api/prestamos/biblioteca/" + idBiblioteca).param("estado", "ACTIVO")
        .header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
    mockMvc.perform(get("/api/prestamos/biblioteca/" + idBiblioteca)
        .header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  @DisplayName("IT3-I-QRY-04: un préstamo inexistente devuelve 404")
  void consulta_inexistente() throws Exception {
    mockMvc.perform(get("/api/prestamos/999999").header("Authorization", tokenBiblio))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("IT3-I-QRY-05: el estado por CI informa los préstamos pendientes del usuario")
  void consulta_estadoPorCi() throws Exception {
    crearPrestamoNuevo(estudiante1);
    String ci = estudiante1.getPersona().getCi();

    mockMvc.perform(get("/api/prestamos/estado-usuario/" + idBiblioteca + "/" + ci)
        .header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.tienePrestamosPendientes").value(true))
        .andExpect(jsonPath("$.data.prestamosActivos").value(1));
    mockMvc.perform(get("/api/prestamos/estado-usuario/" + idBiblioteca + "/0000000")
        .header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.tienePrestamosPendientes").value(false));
  }

  @Test
  @DisplayName("IT3-I-QRY-06: la lista 'por vencer' incluye préstamos que vencen dentro de la ventana")
  void consulta_porVencer() throws Exception {
    long id = crearPrestamoNuevo(estudiante1);
    forzarFechaEstimada(id, -2); // vence en 2 días

    mockMvc.perform(get("/api/prestamos/por-vencer").param("dias", "3").header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
    mockMvc.perform(get("/api/prestamos/por-vencer").param("dias", "1").header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  // ===================== VENCIMIENTO (tarea programada) =====================

  @Test
  @DisplayName("IT3-I-VEN-01: la tarea de vencidos marca VENCIDO un préstamo ACTIVO con fecha pasada")
  void vencimiento_activo() throws Exception {
    long id = crearPrestamoNuevo(estudiante1);
    forzarFechaEstimada(id, 2);

    prestamoService.procesarPrestamosVencidos();

    assertThat(recargarPrestamo(id).getEstadoPrestamo()).isEqualTo(EstadoPrestamo.VENCIDO);
  }

  @Test
  @DisplayName("IT3-I-VEN-02: la tarea de vencidos no toca los préstamos que aún están a tiempo")
  void vencimiento_aTiempo() throws Exception {
    long id = crearPrestamoNuevo(estudiante1);

    prestamoService.procesarPrestamosVencidos();

    assertThat(recargarPrestamo(id).getEstadoPrestamo()).isEqualTo(EstadoPrestamo.ACTIVO);
  }

}
