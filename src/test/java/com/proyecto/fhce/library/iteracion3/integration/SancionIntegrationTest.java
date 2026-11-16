package com.proyecto.fhce.library.iteracion3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.enums.EstadoSancion;
import com.proyecto.fhce.library.enums.MotivoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.services.loads.SancionService;

/**
 * Iteración 3 · Integración de sanciones: del préstamo vencido a la multa,
 * la suspensión (que bloquea nuevos préstamos), el pago y la condonación.
 * Config: Bs 2 por día, tope 7 días, suspensión 15 días.
 */
@DisplayName("Iteración 3 · Sanciones (integración)")
class SancionIntegrationTest extends PrestamoIntegrationBase {

  @Autowired
  SancionService sancionService;

  private MvcResult procesar(String token, long idPrestamo) throws Exception {
    return mockMvc.perform(post("/api/sanciones/prestamo/" + idPrestamo + "/procesar")
        .header("Authorization", token)).andReturn();
  }

  /** Crea un préstamo de estudiante1 con la fecha estimada "diasRetraso" días atrás. */
  private long prestamoAtrasado(int diasRetraso) throws Exception {
    long id = crearPrestamoNuevo(estudiante1);
    forzarFechaEstimada(id, diasRetraso);
    return id;
  }

  private BigDecimal monto(JsonNode n, String campo) {
    return new BigDecimal(n.path(campo).asText());
  }

  private long idSancionDe(MvcResult r) throws Exception {
    assertThat(r.getResponse().getStatus()).as(r.getResponse().getContentAsString()).isEqualTo(201);
    return cuerpo(r).path("idSancion").asLong();
  }

  private MvcResult patchJson(String url, String token, Object body) throws Exception {
    return mockMvc.perform(patch(url).header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON).content(json(body))).andReturn();
  }

  // ===================== GENERACIÓN POR RETRASO =====================

  @Test
  @DisplayName("IT3-I-SAN-01: 3 días de retraso generan una MULTA ACTIVA de Bs 6")
  void retraso_multa() throws Exception {
    long prestamo = prestamoAtrasado(3);

    MvcResult r = procesar(tokenBiblio, prestamo);

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    JsonNode s = cuerpo(r);
    assertThat(s.path("tipoSancion").asText()).isEqualTo("MULTA");
    assertThat(s.path("estado").asText()).isEqualTo("ACTIVA");
    assertThat(s.path("motivo").asText()).isEqualTo("RETRASO_DEVOLUCION");
    assertThat(s.path("diasRetraso").asInt()).isEqualTo(3);
    assertThat(monto(s, "montoMulta")).isEqualByComparingTo("6");
    assertThat(s.path("prestamoId").asLong()).isEqualTo(prestamo);
  }

  @Test
  @DisplayName("IT3-I-SAN-02: 10 días de retraso superan el tope: multa de Bs 14 y suspensión de 15 días")
  void retraso_multaYSuspension() throws Exception {
    long prestamo = prestamoAtrasado(10);

    MvcResult r = procesar(tokenBiblio, prestamo);

    JsonNode s = cuerpo(r);
    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    assertThat(s.path("tipoSancion").asText()).isEqualTo("MULTA_Y_SUSPENSION");
    assertThat(monto(s, "montoMulta")).isEqualByComparingTo("14");
    assertThat(s.path("diasSuspension").asInt()).isEqualTo(15);
    assertThat(s.path("suspensionVigente").asBoolean()).isTrue();
  }

  @Test
  @DisplayName("IT3-I-SAN-03: procesar dos veces el mismo préstamo no duplica la sanción (409)")
  void retraso_duplicada() throws Exception {
    long prestamo = prestamoAtrasado(3);
    procesar(tokenBiblio, prestamo);

    assertThat(procesar(tokenBiblio, prestamo).getResponse().getStatus()).isEqualTo(409);
    assertThat(sancionRepository.contarSancionesActivasPorUsuario(estudiante1.getId_usuario())).isEqualTo(1);
  }

  @Test
  @DisplayName("IT3-I-SAN-04: un préstamo sin retraso no puede sancionarse (409)")
  void retraso_noVencido() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);

    assertThat(procesar(tokenBiblio, prestamo).getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-SAN-05: un ESTUDIANTE no puede generar sanciones (403)")
  void retraso_estudianteProhibido() throws Exception {
    long prestamo = prestamoAtrasado(3);

    assertThat(procesar(tokenEstudiante1, prestamo).getResponse().getStatus()).isEqualTo(403);
  }

  // ===================== SANCIÓN MANUAL =====================

  @Test
  @DisplayName("IT3-I-MAN-01: el bibliotecario registra una sanción manual por daño con monto fijo")
  void manual_ok() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("usuarioId", estudiante1.getId_usuario());
    body.put("bibliotecaId", idBiblioteca);
    body.put("motivo", "DANIO_EJEMPLAR");
    body.put("montoFijo", 50);
    body.put("observaciones", "Páginas arrancadas");

    MvcResult r = mockMvc.perform(post("/api/sanciones/manual").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(body))).andReturn();

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    JsonNode s = cuerpo(r);
    assertThat(s.path("estado").asText()).isEqualTo("ACTIVA");
    assertThat(s.path("motivo").asText()).isEqualTo("DANIO_EJEMPLAR");
    assertThat(monto(s, "montoMulta")).isEqualByComparingTo("50");
  }

  @Test
  @DisplayName("IT3-I-MAN-02: sanción manual sin motivo -> 400")
  void manual_validacion() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("usuarioId", estudiante1.getId_usuario());
    body.put("bibliotecaId", idBiblioteca);

    mockMvc.perform(post("/api/sanciones/manual").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT3-I-MAN-03: un monto negativo es rechazado (400)")
  void manual_montoNegativo() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("usuarioId", estudiante1.getId_usuario());
    body.put("bibliotecaId", idBiblioteca);
    body.put("motivo", "DANIO_EJEMPLAR");
    body.put("montoFijo", -5);

    mockMvc.perform(post("/api/sanciones/manual").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT3-I-MAN-04: un ESTUDIANTE no puede registrar sanciones manuales (403)")
  void manual_estudianteProhibido() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("usuarioId", estudiante2.getId_usuario());
    body.put("bibliotecaId", idBiblioteca);
    body.put("motivo", "DANIO_EJEMPLAR");

    mockMvc.perform(post("/api/sanciones/manual").header("Authorization", tokenEstudiante1)
        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().isForbidden());
  }

  // ===================== PAGO Y CONDONACIÓN =====================

  @Test
  @DisplayName("IT3-I-PAG-01: pagar una multa la pasa a PAGADA con método y fecha de pago")
  void pago_ok() throws Exception {
    long sancion = idSancionDe(procesar(tokenBiblio, prestamoAtrasado(3)));

    MvcResult r = patchJson("/api/sanciones/" + sancion + "/pago", tokenBiblio,
        Map.of("metodoPago", "EFECTIVO"));

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    JsonNode s = cuerpo(r);
    assertThat(s.path("estado").asText()).isEqualTo("PAGADA");
    assertThat(s.path("metodoPago").asText()).isEqualTo("EFECTIVO");
    assertThat(s.path("fechaPago").isNull()).isFalse();
  }

  @Test
  @DisplayName("IT3-I-PAG-02: una sanción ya pagada no se puede pagar de nuevo (409)")
  void pago_doble() throws Exception {
    long sancion = idSancionDe(procesar(tokenBiblio, prestamoAtrasado(3)));
    patchJson("/api/sanciones/" + sancion + "/pago", tokenBiblio, Map.of("metodoPago", "EFECTIVO"));

    assertThat(patchJson("/api/sanciones/" + sancion + "/pago", tokenBiblio, Map.of("metodoPago", "QR"))
        .getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-PAG-03: el método de pago es obligatorio (400)")
  void pago_sinMetodo() throws Exception {
    long sancion = idSancionDe(procesar(tokenBiblio, prestamoAtrasado(3)));

    assertThat(patchJson("/api/sanciones/" + sancion + "/pago", tokenBiblio, Map.of("observaciones", "x"))
        .getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("IT3-I-PAG-04: pagar una sanción inexistente -> 404")
  void pago_inexistente() throws Exception {
    assertThat(patchJson("/api/sanciones/999999/pago", tokenBiblio, Map.of("metodoPago", "EFECTIVO"))
        .getResponse().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("IT3-I-PAG-05: un ESTUDIANTE no puede registrar pagos (403)")
  void pago_estudianteProhibido() throws Exception {
    long sancion = idSancionDe(procesar(tokenBiblio, prestamoAtrasado(3)));

    assertThat(patchJson("/api/sanciones/" + sancion + "/pago", tokenEstudiante1,
        Map.of("metodoPago", "EFECTIVO")).getResponse().getStatus()).isEqualTo(403);
  }

  @Test
  @DisplayName("IT3-I-CON-01: condonar deja la sanción CONDONADA con su motivo")
  void condonar_ok() throws Exception {
    long sancion = idSancionDe(procesar(tokenBiblio, prestamoAtrasado(3)));

    MvcResult r = patchJson("/api/sanciones/" + sancion + "/condonar", tokenBiblio,
        Map.of("observaciones", "Caso de fuerza mayor"));

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    JsonNode s = cuerpo(r);
    assertThat(s.path("estado").asText()).isEqualTo("CONDONADA");
    assertThat(s.path("observaciones").asText()).isEqualTo("Caso de fuerza mayor");
  }

  @Test
  @DisplayName("IT3-I-CON-02: condonar sin motivo -> 400; condonar dos veces -> 409")
  void condonar_validaciones() throws Exception {
    long sancion = idSancionDe(procesar(tokenBiblio, prestamoAtrasado(3)));

    assertThat(patchJson("/api/sanciones/" + sancion + "/condonar", tokenBiblio, Map.of())
        .getResponse().getStatus()).isEqualTo(400);
    patchJson("/api/sanciones/" + sancion + "/condonar", tokenBiblio, Map.of("observaciones", "ok"));
    assertThat(patchJson("/api/sanciones/" + sancion + "/condonar", tokenBiblio, Map.of("observaciones", "ok"))
        .getResponse().getStatus()).isEqualTo(409);
  }

  // ===================== CONSULTAS Y ACCESO =====================

  @Test
  @DisplayName("IT3-I-QRY-01: el estudiante ve su propio historial pero no el de otro usuario")
  void consulta_historialPropio() throws Exception {
    procesar(tokenBiblio, prestamoAtrasado(3));

    mockMvc.perform(get("/api/sanciones/usuario/" + estudiante1.getId_usuario() + "/historial")
        .header("Authorization", tokenEstudiante1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    mockMvc.perform(get("/api/sanciones/usuario/" + estudiante1.getId_usuario() + "/historial")
        .header("Authorization", tokenEstudiante2))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT3-I-QRY-02: el personal lista las sanciones activas de una biblioteca")
  void consulta_porBiblioteca() throws Exception {
    long sancion = idSancionDe(procesar(tokenBiblio, prestamoAtrasado(3)));

    mockMvc.perform(get("/api/sanciones/biblioteca/" + idBiblioteca).header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));

    patchJson("/api/sanciones/" + sancion + "/condonar", tokenBiblio, Map.of("observaciones", "ok"));

    mockMvc.perform(get("/api/sanciones/biblioteca/" + idBiblioteca).header("Authorization", tokenBiblio))
        .andExpect(jsonPath("$.length()").value(0));
    mockMvc.perform(get("/api/sanciones/biblioteca/" + idBiblioteca).param("estado", "CONDONADA")
        .header("Authorization", tokenBiblio))
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  @DisplayName("IT3-I-QRY-03: buscar una sanción inexistente -> 404")
  void consulta_inexistente() throws Exception {
    mockMvc.perform(get("/api/sanciones/999999").header("Authorization", tokenBiblio))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("IT3-I-QRY-04: el estado por CI de un usuario sin sanciones es neutro")
  void consulta_estadoPorCiLimpio() throws Exception {
    mockMvc.perform(get("/api/sanciones/usuario/ci/" + estudiante2.getPersona().getCi() + "/estado")
        .param("bibliotecaId", String.valueOf(idBiblioteca)).header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.tieneSuspensionVigente").value(false))
        .andExpect(jsonPath("$.totalSancionesActivas").value(0));
  }

  // ===================== INTEGRACIÓN PRÉSTAMOS <-> SANCIONES =====================

  @Test
  @DisplayName("IT3-I-INT-01: una suspensión vigente bloquea nuevos préstamos y renovaciones (409)")
  void integracion_suspensionBloquea() throws Exception {
    long vencido = prestamoAtrasado(10);
    long vigente = crearPrestamoNuevo(estudiante1); // otro préstamo a tiempo
    procesar(tokenBiblio, vencido); // MULTA_Y_SUSPENSION

    mockMvc.perform(get("/api/sanciones/usuario/" + estudiante1.getId_usuario() + "/estado")
        .param("bibliotecaId", String.valueOf(idBiblioteca)).header("Authorization", tokenBiblio))
        .andExpect(jsonPath("$.tieneSuspensionVigente").value(true))
        .andExpect(jsonPath("$.tieneDeudaPendiente").value(true));

    MvcResult nuevo = postPrestamo(tokenBiblio,
        cuerpoPrestamo(estudiante1.getId_usuario(), crearEjemplar(), "DOMICILIO"));
    assertThat(nuevo.getResponse().getStatus()).isEqualTo(409);

    MvcResult renovar = mockMvc.perform(post("/api/prestamos/renovar").header("Authorization", tokenEstudiante1)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("prestamoId", vigente)))).andReturn();
    assertThat(renovar.getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-INT-02: al condonar la sanción el usuario puede volver a pedir préstamos")
  void integracion_condonarDesbloquea() throws Exception {
    long sancion = idSancionDe(procesar(tokenBiblio, prestamoAtrasado(10)));
    patchJson("/api/sanciones/" + sancion + "/condonar", tokenBiblio, Map.of("observaciones", "Perdonada"));

    MvcResult nuevo = postPrestamo(tokenBiblio,
        cuerpoPrestamo(estudiante1.getId_usuario(), crearEjemplar(), "DOMICILIO"));

    assertThat(nuevo.getResponse().getStatus()).isEqualTo(201);
  }

  @Test
  @DisplayName("IT3-I-INT-03: una multa sin suspensión no bloquea nuevos préstamos")
  void integracion_multaNoBloquea() throws Exception {
    procesar(tokenBiblio, prestamoAtrasado(3));

    MvcResult nuevo = postPrestamo(tokenBiblio,
        cuerpoPrestamo(estudiante1.getId_usuario(), crearEjemplar(), "DOMICILIO"));

    assertThat(nuevo.getResponse().getStatus()).isEqualTo(201);
  }

  // ===================== TAREAS PROGRAMADAS =====================

  @Test
  @DisplayName("IT3-I-JOB-01: la tarea de generación crea la sanción de un préstamo vencido sin sanción")
  void job_generaSancion() throws Exception {
    long prestamo = prestamoAtrasado(3);

    sancionService.procesarPrestamosVencidos();

    em.flush();
    em.clear();
    assertThat(sancionRepository.existsByPrestamo_IdPrestamoAndMotivoAndEstado(prestamo,
        MotivoSancion.RETRASO_DEVOLUCION, EstadoSancion.ACTIVA)).isTrue();
  }

  @Test
  @DisplayName("IT3-I-JOB-02: la tarea de generación es idempotente (ejecutarla dos veces no duplica)")
  void job_idempotente() throws Exception {
    prestamoAtrasado(3);

    sancionService.procesarPrestamosVencidos();
    sancionService.procesarPrestamosVencidos();

    assertThat(sancionRepository.contarSancionesActivasPorUsuario(estudiante1.getId_usuario())).isEqualTo(1);
  }

  @Test
  @DisplayName("IT3-I-JOB-03: la tarea de multas actualiza el monto a medida que pasan los días")
  void job_actualizaMulta() throws Exception {
    long prestamo = prestamoAtrasado(1);
    long sancion = idSancionDe(procesar(tokenBiblio, prestamo)); // Bs 2
    forzarFechaEstimada(prestamo, 3); // pasan 2 días más

    sancionService.actualizarMultasSanciones();

    em.flush();
    em.clear();
    Sancion s = sancionRepository.findById(sancion).orElseThrow();
    assertThat(s.getMontoMulta()).isEqualByComparingTo("6");
    assertThat(s.getDiasRetraso()).isEqualTo(3);
    assertThat(s.getTipoSancion()).isEqualTo(TipoSancion.MULTA);
  }

  @Test
  @DisplayName("IT3-I-JOB-04: la tarea de suspensiones cierra (PAGADA) las suspensiones vencidas")
  void job_cierraSuspension() throws Exception {
    Sancion s = new Sancion();
    s.setUsuario(estudiante1);
    s.setBiblioteca(bibliotecaRepository.findById(idBiblioteca).orElseThrow());
    s.setTipoSancion(TipoSancion.SUSPENSION);
    s.setMotivo(MotivoSancion.REINCIDENCIA);
    s.setFechaInicioSuspension(java.time.LocalDate.now().minusDays(20));
    s.setFechaFinSuspension(java.time.LocalDate.now().minusDays(1));
    Long id = sancionRepository.saveAndFlush(s).getIdSancion();
    em.clear();

    sancionService.cerrarSuspensionesVencidas();

    em.flush();
    em.clear();
    assertThat(sancionRepository.findById(id).orElseThrow().getEstado()).isEqualTo(EstadoSancion.PAGADA);
  }
}
