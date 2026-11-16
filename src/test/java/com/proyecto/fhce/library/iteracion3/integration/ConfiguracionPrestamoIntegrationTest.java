package com.proyecto.fhce.library.iteracion3.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.proyecto.fhce.library.entities.Biblioteca;

/**
 * Iteración 3 · Integración de la configuración de préstamos por biblioteca
 * (días, renovaciones, límites, multa, suspensión). La biblioteca del
 * escenario base ya tiene configuración; las altas usan otra biblioteca.
 */
@DisplayName("Iteración 3 · Configuración de préstamos (integración)")
class ConfiguracionPrestamoIntegrationTest extends PrestamoIntegrationBase {

  private Map<String, Object> cuerpoConfig(Long bibliotecaId) {
    Map<String, Object> m = new HashMap<>();
    m.put("bibliotecaId", bibliotecaId);
    m.put("diasPrestamoMax", 10);
    m.put("renovacionesMax", 1);
    m.put("ejemplaresMaxDomicilio", 4);
    m.put("ejemplaresMaxSala", 6);
    m.put("multaPorDia", 3.5);
    m.put("multaMaxDias", 5);
    m.put("diasSuspension", 20);
    m.put("diasReserva", 3);
    return m;
  }

  private MvcResult crear(String token, Map<String, Object> body) throws Exception {
    return mockMvc.perform(post("/api/configuraciones-prestamo").header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON).content(json(body))).andReturn();
  }

  private Long otraBiblioteca() {
    Biblioteca b = nuevaBiblioteca("Biblioteca Carrera");
    em.flush();
    return b.getIdBiblioteca();
  }

  @Test
  @DisplayName("IT3-I-CFG-01: se crea la configuración de una biblioteca (201) con todos sus valores")
  void crear_ok() throws Exception {
    Long b2 = otraBiblioteca();

    MvcResult r = crear(tokenBiblio, cuerpoConfig(b2));

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    assertThat(cuerpo(r).path("diasPrestamoMax").asInt()).isEqualTo(10);
    assertThat(cuerpo(r).path("ejemplaresMaxDomicilio").asInt()).isEqualTo(4);
    assertThat(cuerpo(r).path("bibliotecaId").asLong()).isEqualTo(b2);
    assertThat(cuerpo(r).path("nombreBiblioteca").asText()).isEqualTo("Biblioteca Carrera");
  }

  @Test
  @DisplayName("IT3-I-CFG-02: una biblioteca solo puede tener una configuración (409)")
  void crear_duplicada() throws Exception {
    assertThat(crear(tokenBiblio, cuerpoConfig(idBiblioteca)).getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT3-I-CFG-03: datos inválidos (días de préstamo 0 o biblioteca ausente) -> 400")
  void crear_validacion() throws Exception {
    Long b2 = otraBiblioteca();
    Map<String, Object> diasCero = cuerpoConfig(b2);
    diasCero.put("diasPrestamoMax", 0);
    Map<String, Object> sinBiblioteca = cuerpoConfig(null);

    assertThat(crear(tokenBiblio, diasCero).getResponse().getStatus()).isEqualTo(400);
    assertThat(crear(tokenBiblio, sinBiblioteca).getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("IT3-I-CFG-04: valores negativos (multa, renovaciones) -> 400")
  void crear_negativos() throws Exception {
    Long b2 = otraBiblioteca();
    Map<String, Object> multaNeg = cuerpoConfig(b2);
    multaNeg.put("multaPorDia", -1);
    Map<String, Object> renovNeg = cuerpoConfig(b2);
    renovNeg.put("renovacionesMax", -1);

    assertThat(crear(tokenBiblio, multaNeg).getResponse().getStatus()).isEqualTo(400);
    assertThat(crear(tokenBiblio, renovNeg).getResponse().getStatus()).isEqualTo(400);
  }

  @Test
  @DisplayName("IT3-I-CFG-05: crear para una biblioteca inexistente -> 404")
  void crear_bibliotecaInexistente() throws Exception {
    assertThat(crear(tokenBiblio, cuerpoConfig(999999L)).getResponse().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("IT3-I-CFG-06: se consulta por id y por biblioteca")
  void consultar() throws Exception {
    mockMvc.perform(get("/api/configuraciones-prestamo/" + idConfig).header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.diasPrestamoMax").value(7))
        .andExpect(jsonPath("$.renovacionesMax").value(2));
    mockMvc.perform(get("/api/configuraciones-prestamo/biblioteca/" + idBiblioteca)
        .header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.idConfig").value(idConfig));
  }

  @Test
  @DisplayName("IT3-I-CFG-07: consultar una configuración inexistente -> 404")
  void consultar_inexistente() throws Exception {
    mockMvc.perform(get("/api/configuraciones-prestamo/999999").header("Authorization", tokenBiblio))
        .andExpect(status().isNotFound());
    mockMvc.perform(get("/api/configuraciones-prestamo/biblioteca/999999").header("Authorization", tokenBiblio))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("IT3-I-CFG-08: se actualiza la configuración y el cambio queda persistido")
  void actualizar() throws Exception {
    Map<String, Object> body = cuerpoConfig(idBiblioteca);
    body.put("diasPrestamoMax", 14);

    mockMvc.perform(put("/api/configuraciones-prestamo/" + idConfig).header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.diasPrestamoMax").value(14));

    em.flush();
    em.clear();
    assertThat(configuracionRepository.findById(idConfig).orElseThrow().getDiasPrestamoMax()).isEqualTo(14);
  }

  @Test
  @DisplayName("IT3-I-CFG-09: se elimina la configuración (204) y deja de existir")
  void eliminar() throws Exception {
    mockMvc.perform(delete("/api/configuraciones-prestamo/" + idConfig).header("Authorization", tokenBiblio))
        .andExpect(status().isNoContent());
    mockMvc.perform(get("/api/configuraciones-prestamo/" + idConfig).header("Authorization", tokenBiblio))
        .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("IT3-I-CFG-10: las reglas de DOMICILIO y SALA devuelven su propio límite de ejemplares")
  void reglas() throws Exception {
    mockMvc.perform(get("/api/configuraciones-prestamo/biblioteca/" + idBiblioteca + "/reglas")
        .param("tipoPrestamo", "DOMICILIO").header("Authorization", tokenBiblio))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ejemplaresPermitidos").value(2))
        .andExpect(jsonPath("$.diasPrestamoMax").value(7));
    mockMvc.perform(get("/api/configuraciones-prestamo/biblioteca/" + idBiblioteca + "/reglas")
        .param("tipoPrestamo", "SALA").header("Authorization", tokenBiblio))
        .andExpect(jsonPath("$.ejemplaresPermitidos").value(3));
  }

  @Test
  @DisplayName("IT3-I-CFG-11: sin autenticación no se accede a la configuración")
  void sinToken() throws Exception {
    mockMvc.perform(get("/api/configuraciones-prestamo/" + idConfig)).andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("IT3-I-CFG-12: un cambio en la configuración afecta a los nuevos préstamos")
  void cambioAfectaPrestamos() throws Exception {
    Map<String, Object> body = cuerpoConfig(idBiblioteca);
    body.put("ejemplaresMaxDomicilio", 1);
    mockMvc.perform(put("/api/configuraciones-prestamo/" + idConfig).header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(body))).andExpect(status().isOk());

    crearPrestamoNuevo(estudiante1);
    MvcResult segundo = postPrestamo(tokenBiblio,
        cuerpoPrestamo(estudiante1.getId_usuario(), crearEjemplar(), "DOMICILIO"));

    assertThat(segundo.getResponse().getStatus()).isEqualTo(409);
  }

  /**
   * DEF-06 (defecto detectado): en {@code ConfiguracionPrestamoController} las
   * anotaciones {@code @PreAuthorize} de crear/editar/eliminar están comentadas.
   * Cualquier usuario autenticado (incluso un ESTUDIANTE) puede modificar las
   * reglas de préstamo y multas de una biblioteca.
   */
  @Test
  // @Disabled("DEF-06: ConfiguracionPrestamoController no restringe
  // crear/editar/eliminar por rol")
  @DisplayName("IT3-I-CFG-13: un ESTUDIANTE no puede crear, editar ni eliminar configuraciones (403)")
  void estudianteNoPuedeModificar() throws Exception {
    Long b2 = otraBiblioteca();

    assertThat(crear(tokenEstudiante1, cuerpoConfig(b2)).getResponse().getStatus()).isEqualTo(403);
    mockMvc.perform(put("/api/configuraciones-prestamo/" + idConfig).header("Authorization", tokenEstudiante1)
        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpoConfig(idBiblioteca))))
        .andExpect(status().isForbidden());
    mockMvc.perform(delete("/api/configuraciones-prestamo/" + idConfig).header("Authorization", tokenEstudiante1))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT3-I-CFG-14: calcular-multa por biblioteca devuelve días x multa por día")
  void calcularMulta() throws Exception {

    MvcResult r = mockMvc.perform(
        get("/api/configuraciones-prestamo/" + idBiblioteca + "/calcular-multa")
            .param("diasRetraso", "3")
            .header("Authorization", tokenBiblio))
        .andReturn();

    assertThat(r.getResponse().getStatus()).isEqualTo(200);

    assertThat(new BigDecimal(r.getResponse().getContentAsString()))
        .isEqualByComparingTo("6");
  }
  // MvcResult r = mockMvc.perform(get("/api/configuraciones-prestamo/" + idConfig
  // + "/calcular-multa")
  // .param("diasRetraso", "3").header("Authorization", tokenBiblio)).andReturn();
}
