package com.proyecto.fhce.library.iteracion4.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.proyecto.fhce.library.enums.EstadoSolicitud;

/** Iteración 4 · Solicitudes y razones de certificado (API + seguridad + JPA sobre H2). */
@DisplayName("Iteración 4 · Solicitudes y razones de certificado (integración)")
class SolicitudCertificadoIntegrationTest extends CertificadoIntegrationBase {

  // ===================== SOLICITUDES =====================

  @Test
  @DisplayName("IT4-I-SOL-01: una persona sin sesión puede enviar una solicitud (201, PENDIENTE) y queda vinculada a su usuario por CI")
  void crear_publica() throws Exception {
    MvcResult r = postSolicitud(null, cuerpoSolicitud(estudiante1));

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    assertThat(cuerpo(r).path("data").path("estado").asText()).isEqualTo("PENDIENTE");
    assertThat(cuerpo(r).path("data").path("usuarioId").asLong()).isEqualTo(estudiante1.getId_usuario());
    assertThat(cuerpo(r).path("data").path("razonNombre").asText()).isEqualTo("Titulación");
  }

  @Test
  @DisplayName("IT4-I-SOL-02: no se permite una segunda solicitud PENDIENTE del mismo CI y razón (409)")
  void crear_duplicada() throws Exception {
    crearSolicitud(estudiante1);

    assertThat(postSolicitud(tokenEstudiante1, cuerpoSolicitud(estudiante1)).getResponse().getStatus())
        .isEqualTo(409);
  }

  @Test
  @DisplayName("IT4-I-SOL-03: tras resolver la primera, se puede volver a solicitar")
  void crear_despuesDeResolver() throws Exception {
    long primera = crearSolicitud(estudiante1);
    resolverSolicitud(tokenBiblio, primera, "rechazar");

    assertThat(postSolicitud(tokenEstudiante1, cuerpoSolicitud(estudiante1)).getResponse().getStatus())
        .isEqualTo(201);
  }

  @Test
  @DisplayName("IT4-I-SOL-04: razón o biblioteca inexistente -> 404")
  void crear_inexistentes() throws Exception {
    Map<String, Object> sinRazon = cuerpoSolicitud(estudiante1);
    sinRazon.put("razonCertificadoId", 999999L);
    Map<String, Object> sinBiblioteca = cuerpoSolicitud(estudiante1);
    sinBiblioteca.put("bibliotecaId", 999999L);

    assertThat(postSolicitud(null, sinRazon).getResponse().getStatus()).isEqualTo(404);
    assertThat(postSolicitud(null, sinBiblioteca).getResponse().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("IT4-I-SOL-05: el bibliotecario lista solicitudes por biblioteca y estado; el estudiante no puede (403)")
  void listarPorBiblioteca() throws Exception {
    long pendiente = crearSolicitud(estudiante1);
    long aprobada = crearSolicitud(estudiante2);
    resolverSolicitud(tokenBiblio, aprobada, "aprobar");

    mockMvc.perform(get("/api/solicitudes-certificado/biblioteca/" + idBiblioteca)
        .header("Authorization", tokenBiblio))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2));
    mockMvc.perform(get("/api/solicitudes-certificado/biblioteca/" + idBiblioteca).param("estado", "PENDIENTE")
        .header("Authorization", tokenBiblio))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].id").value(pendiente));
    mockMvc.perform(get("/api/solicitudes-certificado/biblioteca/" + idBiblioteca)
        .header("Authorization", tokenEstudiante1))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT4-I-SOL-06: aprobar deja la solicitud APROBADA y registra quién la atendió")
  void aprobar() throws Exception {
    long id = crearSolicitud(estudiante1);

    MvcResult r = resolverSolicitud(tokenBiblio, id, "aprobar");

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(cuerpo(r).path("data").path("estado").asText()).isEqualTo("APROBADA");
    assertThat(cuerpo(r).path("data").path("atendidoPorId").asLong()).isEqualTo(biblio.getId_usuario());
    assertThat(cuerpo(r).path("data").path("observacionRespuesta").asText()).isEqualTo("Revisado");
  }

  @Test
  @DisplayName("IT4-I-SOL-07: rechazar deja la solicitud RECHAZADA")
  void rechazar() throws Exception {
    long id = crearSolicitud(estudiante1);

    MvcResult r = resolverSolicitud(tokenBiblio, id, "rechazar");

    assertThat(cuerpo(r).path("data").path("estado").asText()).isEqualTo("RECHAZADA");
  }

  @Test
  @DisplayName("IT4-I-SOL-08: un ESTUDIANTE no puede aprobar ni rechazar (403)")
  void resolver_estudianteProhibido() throws Exception {
    long id = crearSolicitud(estudiante1);

    assertThat(resolverSolicitud(tokenEstudiante1, id, "aprobar").getResponse().getStatus()).isEqualTo(403);
    assertThat(resolverSolicitud(tokenEstudiante1, id, "rechazar").getResponse().getStatus()).isEqualTo(403);
    assertThat(solicitudRepository.findById(id).orElseThrow().getEstado()).isEqualTo(EstadoSolicitud.PENDIENTE);
  }

  @Test
  @DisplayName("IT4-I-SOL-09: aprobar una solicitud inexistente -> 404")
  void aprobar_inexistente() throws Exception {
    assertThat(resolverSolicitud(tokenBiblio, 999999L, "aprobar").getResponse().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("IT4-I-SOL-10: 'mis solicitudes' devuelve solo las del estudiante (paginado)")
  void misSolicitudes() throws Exception {
    crearSolicitud(estudiante1);
    crearSolicitud(estudiante2);

    mockMvc.perform(get("/api/solicitudes-certificado/mis-solicitudes").header("Authorization", tokenEstudiante1))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.content.length()").value(1))
        .andExpect(jsonPath("$.data.content[0].usuarioId").value(estudiante1.getId_usuario()));
    mockMvc.perform(get("/api/solicitudes-certificado/mis-solicitudes").header("Authorization", tokenBiblio))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT4-I-SOL-11: el listado global de solicitudes es solo para ADMIN")
  void listarTodas() throws Exception {
    crearSolicitud(estudiante1);

    mockMvc.perform(get("/api/solicitudes-certificado/todas").header("Authorization", tokenBiblio))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/solicitudes-certificado/todas").header("Authorization", tokenAdmin))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.content.length()").value(1));
  }

  @Test
  @DisplayName("IT4-I-SOL-12: el solicitante consulta su solicitud por id; sin sesión no se puede")
  void obtenerPorId() throws Exception {
    long id = crearSolicitud(estudiante1);

    mockMvc.perform(get("/api/solicitudes-certificado/" + id).header("Authorization", tokenEstudiante1))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id));
    mockMvc.perform(get("/api/solicitudes-certificado/" + id)).andExpect(status().is4xxClientError());
    mockMvc.perform(get("/api/solicitudes-certificado/999999").header("Authorization", tokenAdmin))
        .andExpect(status().isNotFound());
  }

  /**
   * DEF-12 (defecto detectado): {@code SolicitudCertificadoRequest} no tiene
   * anotaciones de validación y el endpoint es público. Una solicitud con el
   * cuerpo vacío provoca una excepción interna (HTTP 500) en lugar de un 400
   * con los campos faltantes.
   */
  @Test
  @Disabled("DEF-12: SolicitudCertificadoRequest sin validaciones: cuerpo vacío -> 500")
  @DisplayName("IT4-I-SOL-13: una solicitud sin datos obligatorios devuelve 400")
  void crear_validacion() throws Exception {
    assertThat(postSolicitud(null, new HashMap<>()).getResponse().getStatus()).isEqualTo(400);
    Map<String, Object> sinCi = cuerpoSolicitud(estudiante1);
    sinCi.remove("ci");
    assertThat(postSolicitud(null, sinCi).getResponse().getStatus()).isEqualTo(400);
  }

  /**
   * DEF-15 (defecto detectado): {@code GET /api/solicitudes-certificado/{id}}
   * solo exige estar autenticado y el servicio ignora al solicitante. Cualquier
   * estudiante puede leer solicitudes ajenas (CI, correo, teléfono, matrícula).
   */
  @Test
  @Disabled("DEF-15: cualquier usuario autenticado puede leer solicitudes de otras personas")
  @DisplayName("IT4-I-SOL-14: un estudiante no puede ver la solicitud de otro estudiante (403)")
  void obtenerPorId_ajena() throws Exception {
    long id = crearSolicitud(estudiante1);

    mockMvc.perform(get("/api/solicitudes-certificado/" + id).header("Authorization", tokenEstudiante2))
        .andExpect(status().isForbidden());
  }

  // ===================== RAZONES =====================

  private Map<String, Object> cuerpoRazon() {
    Map<String, Object> m = new HashMap<>();
    m.put("bibliotecaId", idBiblioteca);
    m.put("nombre", "Beca");
    m.put("descripcion", "Trámite de beca");
    m.put("requisitos", List.of("CI", "Matrícula"));
    m.put("activo", true);
    return m;
  }

  @Test
  @DisplayName("IT4-I-RAZ-01: el listado de razones es público (sin token) y se filtra por biblioteca")
  void razon_listadoPublico() throws Exception {
    mockMvc.perform(get("/api/razones-certificado")).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));
    mockMvc.perform(get("/api/razones-certificado/biblioteca/" + idBiblioteca)).andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].nombre").value("Titulación"));
    mockMvc.perform(get("/api/razones-certificado/" + idRazon)).andExpect(status().isOk())
        .andExpect(jsonPath("$.data.activo").value(true));
  }

  @Test
  @DisplayName("IT4-I-RAZ-02: el bibliotecario crea una razón (201) con sus requisitos")
  void razon_crear() throws Exception {
    mockMvc.perform(post("/api/razones-certificado").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpoRazon())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.nombre").value("Beca"))
        .andExpect(jsonPath("$.data.bibliotecaId").value(idBiblioteca));
    mockMvc.perform(get("/api/razones-certificado/biblioteca/" + idBiblioteca))
        .andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  @DisplayName("IT4-I-RAZ-03: un ESTUDIANTE o un anónimo no pueden crear, editar ni eliminar razones")
  void razon_permisos() throws Exception {
    mockMvc.perform(post("/api/razones-certificado").header("Authorization", tokenEstudiante1)
        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpoRazon())))
        .andExpect(status().isForbidden());
    mockMvc.perform(put("/api/razones-certificado/" + idRazon).header("Authorization", tokenEstudiante1)
        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpoRazon())))
        .andExpect(status().isForbidden());
    mockMvc.perform(delete("/api/razones-certificado/" + idRazon).header("Authorization", tokenEstudiante1))
        .andExpect(status().isForbidden());
    mockMvc.perform(post("/api/razones-certificado").contentType(MediaType.APPLICATION_JSON)
        .content(json(cuerpoRazon()))).andExpect(status().is4xxClientError());
  }

  @Test
  @DisplayName("IT4-I-RAZ-04: se actualiza una razón (nombre y estado) y el cambio queda guardado")
  void razon_actualizar() throws Exception {
    Map<String, Object> body = cuerpoRazon();
    body.put("nombre", "Titulación 2026");
    body.put("activo", false);

    mockMvc.perform(put("/api/razones-certificado/" + idRazon).header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(body)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.nombre").value("Titulación 2026"))
        .andExpect(jsonPath("$.data.activo").value(false));
  }

  @Test
  @DisplayName("IT4-I-RAZ-05: se elimina una razón sin solicitudes y deja de existir")
  void razon_eliminar() throws Exception {
    mockMvc.perform(delete("/api/razones-certificado/" + idRazon).header("Authorization", tokenAdmin))
        .andExpect(status().isOk());
    mockMvc.perform(get("/api/razones-certificado/biblioteca/" + idBiblioteca))
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  /**
   * DEF-13 (defecto detectado): {@code RazonCertificadoServiceImpl} lanza
   * {@code RuntimeException} cuando no encuentra la razón y el manejador global
   * la responde como 500. Debe ser 404.
   */
  @Test
  @Disabled("DEF-13: razón inexistente responde 500 en vez de 404")
  @DisplayName("IT4-I-RAZ-06: consultar o editar una razón inexistente devuelve 404")
  void razon_inexistente() throws Exception {
    mockMvc.perform(get("/api/razones-certificado/999999")).andExpect(status().isNotFound());
    mockMvc.perform(put("/api/razones-certificado/999999").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpoRazon())))
        .andExpect(status().isNotFound());
  }

  /**
   * DEF-12 (parte razones): {@code RazonCertificadoRequestDTO} no valida nada;
   * crear una razón sin biblioteca ni nombre termina en 500.
   */
  @Test
  @Disabled("DEF-12: RazonCertificadoRequestDTO sin validaciones: datos faltantes -> 500")
  @DisplayName("IT4-I-RAZ-07: crear una razón sin nombre o sin biblioteca devuelve 400")
  void razon_validacion() throws Exception {
    Map<String, Object> sinNombre = cuerpoRazon();
    sinNombre.remove("nombre");
    Map<String, Object> sinBiblioteca = cuerpoRazon();
    sinBiblioteca.remove("bibliotecaId");

    mockMvc.perform(post("/api/razones-certificado").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(sinNombre))).andExpect(status().isBadRequest());
    mockMvc.perform(post("/api/razones-certificado").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON).content(json(sinBiblioteca))).andExpect(status().isBadRequest());
  }
}
