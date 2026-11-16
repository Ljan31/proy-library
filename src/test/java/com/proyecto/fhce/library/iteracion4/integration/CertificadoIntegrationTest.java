package com.proyecto.fhce.library.iteracion4.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.proyecto.fhce.library.entities.BibliotecaEncargado;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;
import com.proyecto.fhce.library.entities.Sancion;
import com.proyecto.fhce.library.enums.EstadoCertificado;
import com.proyecto.fhce.library.enums.MotivoSancion;
import com.proyecto.fhce.library.enums.TipoSancion;
import com.proyecto.fhce.library.services.loads.CertificadoNoDeudaService;

/**
 * Iteración 4 · Certificados de No Deuda de punta a punta:
 * solicitud -> aprobación -> certificado -> validación pública ->
 * anulación/vencimiento,
 * con el bloqueo por deudas de préstamos (la descarga del PDF queda fuera:
 * requiere LibreOffice).
 */
@DisplayName("Iteración 4 · Certificados de No Deuda (integración)")
class CertificadoIntegrationTest extends CertificadoIntegrationBase {

  @Autowired
  CertificadoNoDeudaService certificadoService;

  private MvcResult patchAnular(String token, long id) throws Exception {
    return mockMvc.perform(patch("/api/certificados/" + id + "/anular").header("Authorization", token))
        .andReturn();
  }

  private MvcResult validar(String codigo) throws Exception {
    return mockMvc.perform(get("/api/certificados/validar/" + codigo)).andReturn(); // sin token
  }

  private String codigoDe(long idCertificado) {
    em.flush();
    em.clear();
    return certificadoRepository.findById(idCertificado).orElseThrow().getCodigoVerificacion();
  }

  private void devolver(long idPrestamo) throws Exception {
    mockMvc.perform(post("/api/prestamos/devolucion").header("Authorization", tokenBiblio)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(Map.of("prestamoId", idPrestamo, "condicionDevolucion", "BUENO"))))
        .andExpect(status().isOk());
  }

  // ===================== EMISIÓN =====================

  @Test
  @DisplayName("IT4-I-CER-01: flujo completo: solicitud aprobada -> certificado VIGENTE con código y enlace de descarga")
  void flujoCompleto() throws Exception {
    long solicitud = solicitudAprobada(estudiante1);

    MvcResult r = postCertificado(tokenBiblio, cuerpoCertificado(solicitud));

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    var c = cuerpo(r).path("data");
    assertThat(c.path("estadoCertificado").asText()).isEqualTo("VIGENTE");
    assertThat(c.path("codigo_verificacion").asText()).isNotBlank();
    assertThat(c.path("solicitudId").asLong()).isEqualTo(solicitud);
    assertThat(c.path("bibliotecaId").asLong()).isEqualTo(idBiblioteca);
    assertThat(c.path("bibliotecario").path("id_usuario").asLong()).isEqualTo(biblio.getId_usuario());
    assertThat(c.path("urlDescarga").asText()).endsWith("/download");
    assertThat(c.path("fechaVencimiento").isNull()).isTrue();
  }

  @Test
  @DisplayName("IT4-I-CER-02: con días de validez el certificado queda con fecha de vencimiento")
  void conDiasDeValidez() throws Exception {
    long solicitud = solicitudAprobada(estudiante1);
    Map<String, Object> body = cuerpoCertificado(solicitud);
    body.put("diasValidez", 30);

    MvcResult r = postCertificado(tokenBiblio, body);

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    assertThat(cuerpo(r).path("data").path("fechaVencimiento").isNull()).isFalse();
  }

  @Test
  @DisplayName("IT4-I-CER-03: una solicitud PENDIENTE no genera certificado (409)")
  void solicitudPendiente() throws Exception {
    long solicitud = crearSolicitud(estudiante1);

    assertThat(postCertificado(tokenBiblio, cuerpoCertificado(solicitud)).getResponse().getStatus())
        .isEqualTo(409);
  }

  @Test
  @DisplayName("IT4-I-CER-04: una solicitud RECHAZADA tampoco (409)")
  void solicitudRechazada() throws Exception {
    long solicitud = crearSolicitud(estudiante1);
    resolverSolicitud(tokenBiblio, solicitud, "rechazar");

    assertThat(postCertificado(tokenBiblio, cuerpoCertificado(solicitud)).getResponse().getStatus())
        .isEqualTo(409);
  }

  @Test
  @DisplayName("IT4-I-CER-05: una solicitud solo puede generar un certificado (409 al repetir)")
  void certificadoUnicoPorSolicitud() throws Exception {
    long solicitud = solicitudAprobada(estudiante1);
    assertThat(postCertificado(tokenBiblio, cuerpoCertificado(solicitud)).getResponse().getStatus())
        .isEqualTo(201);

    assertThat(postCertificado(tokenBiblio, cuerpoCertificado(solicitud)).getResponse().getStatus())
        .isEqualTo(409);
  }

  @Test
  @DisplayName("IT4-I-CER-06: un ESTUDIANTE no puede generar certificados (403)")
  void estudianteProhibido() throws Exception {
    long solicitud = solicitudAprobada(estudiante1);

    assertThat(postCertificado(tokenEstudiante1, cuerpoCertificado(solicitud)).getResponse().getStatus())
        .isEqualTo(403);
  }

  @Test
  @DisplayName("IT4-I-CER-07: un bibliotecario que no está a cargo de la biblioteca no puede generar (409)")
  void bibliotecarioAjeno() throws Exception {
    long solicitud = solicitudAprobada(estudiante1);

    assertThat(postCertificado(tokenOtroBiblio, cuerpoCertificado(solicitud)).getResponse().getStatus())
        .isEqualTo(409);
  }

  @Test
  @DisplayName("IT4-I-CER-08: el administrador genera un certificado directo, sin solicitud previa")
  void directoAdmin() throws Exception {
    MvcResult r = postCertificado(tokenAdmin, cuerpoCertificadoDirecto(estudiante1));

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    assertThat(cuerpo(r).path("data").path("usuario").path("id_usuario").asLong())
        .isEqualTo(estudiante1.getId_usuario());
    // dejó una solicitud APROBADA asociada
    assertThat(solicitudRepository.findByUsuario_IdUsuario(estudiante1.getId_usuario())).hasSize(1);
  }

  @Test
  @DisplayName("IT4-I-CER-09: certificado directo para una persona sin cuenta (por nombres, apellidos y CI)")
  void directoSinCuenta() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("bibliotecaId", idBiblioteca);
    body.put("razonCertificadoId", idRazon);
    body.put("nombres", "Ana");
    body.put("apellidos", "Lopez Rojas");
    body.put("ci", "7654321");

    MvcResult r = postCertificado(tokenAdmin, body);

    assertThat(r.getResponse().getStatus()).isEqualTo(201);
    mockMvc.perform(get("/api/certificados/usuarioCi").param("ci", "7654321")
        .param("bibliotecaId", String.valueOf(idBiblioteca)).header("Authorization", tokenBiblio))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("IT4-I-CER-10: certificado directo sin usuario ni datos de la persona -> 409")
  void directoSinDatos() throws Exception {
    Map<String, Object> body = new HashMap<>();
    body.put("bibliotecaId", idBiblioteca);
    body.put("razonCertificadoId", idRazon);

    assertThat(postCertificado(tokenAdmin, body).getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT4-I-CER-11: el cuerpo sin biblioteca es rechazado (400)")
  void sinBiblioteca() throws Exception {
    mockMvc.perform(post("/api/certificados").header("Authorization", tokenAdmin)
        .contentType(MediaType.APPLICATION_JSON).content("{\"usuarioId\":1}"))
        .andExpect(status().isBadRequest());
  }

  // ===================== NO DEUDA (préstamos) =====================

  @Test
  @DisplayName("IT4-I-DEU-01: con un préstamo ACTIVO no se emite el certificado; al devolverlo sí")
  void bloqueoPorPrestamoActivo() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);

    MvcResult bloqueado = postCertificado(tokenAdmin, cuerpoCertificadoDirecto(estudiante1));
    assertThat(bloqueado.getResponse().getStatus()).isEqualTo(409);
    assertThat(bloqueado.getResponse().getContentAsString()).contains("préstamo(s) activo(s)");

    devolver(prestamo);
    assertThat(postCertificado(tokenAdmin, cuerpoCertificadoDirecto(estudiante1)).getResponse().getStatus())
        .isEqualTo(201);
  }

  @Test
  @DisplayName("IT4-I-DEU-02: con un préstamo RENOVADO pendiente tampoco se emite (409)")
  void bloqueoPorPrestamoRenovado() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    mockMvc.perform(post("/api/prestamos/renovar").header("Authorization", tokenEstudiante1)
        .contentType(MediaType.APPLICATION_JSON).content(json(Map.of("prestamoId", prestamo))))
        .andExpect(status().isOk());

    MvcResult r = postCertificado(tokenAdmin, cuerpoCertificadoDirecto(estudiante1));

    assertThat(r.getResponse().getStatus()).isEqualTo(409);
    assertThat(r.getResponse().getContentAsString()).contains("renovación pendiente");
  }

  @Test
  @DisplayName("IT4-I-DEU-03: con un préstamo VENCIDO tampoco se emite (409)")
  void bloqueoPorPrestamoVencido() throws Exception {
    long prestamo = crearPrestamoNuevo(estudiante1);
    forzarFechaEstimada(prestamo, 3);
    forzarEstado(prestamo, com.proyecto.fhce.library.enums.EstadoPrestamo.VENCIDO);

    MvcResult r = postCertificado(tokenAdmin, cuerpoCertificadoDirecto(estudiante1));

    assertThat(r.getResponse().getStatus()).isEqualTo(409);
    assertThat(r.getResponse().getContentAsString()).contains("vencido");
  }

  @Test
  @DisplayName("IT4-I-DEU-04: los préstamos de OTRA biblioteca no bloquean el certificado de esta")
  void deudaEsPorBiblioteca() throws Exception {
    // el estudiante1 pide un préstamo en la biblioteca base; el certificado se pide
    // en otra
    crearPrestamoNuevo(estudiante1);
    var otra = nuevaBiblioteca("Biblioteca Carrera");
    var razonOtra = new com.proyecto.fhce.library.entities.RazonCertificado();
    razonOtra.setBiblioteca(otra);
    razonOtra.setNombre("Beca");
    razonOtra.setRequisitos("[]");
    razonOtra.setActivo(true);
    Long idRazonOtra = razonRepository.saveAndFlush(razonOtra).getIdRazon();
    Map<String, Object> body = new HashMap<>();
    body.put("bibliotecaId", otra.getIdBiblioteca());
    body.put("usuarioId", estudiante1.getId_usuario());
    body.put("razonCertificadoId", idRazonOtra);

    assertThat(postCertificado(tokenAdmin, body).getResponse().getStatus()).isEqualTo(201);
  }

  /**
   * DEF-11 (defecto detectado): un certificado de NO DEUDA se emite aunque el
   * usuario tenga sanciones activas (multas impagas). La validación de
   * sanciones está comentada en {@code CertificadoNoDeudaServiceImpl.generar}
   * y {@code validarDeudas} solo revisa préstamos.
   */
  @Test
  // @Disabled("DEF-11: se emite el certificado de no deuda aunque el usuario
  // tenga multas/sanciones activas")
  @DisplayName("IT4-I-DEU-05: con una multa activa impaga no se emite el certificado (409)")
  void bloqueoPorSancionActiva() throws Exception {
    Sancion s = new Sancion();
    s.setUsuario(estudiante1);
    s.setBiblioteca(bibliotecaRepository.findById(idBiblioteca).orElseThrow());
    s.setTipoSancion(TipoSancion.MULTA);
    s.setMotivo(MotivoSancion.DANIO_EJEMPLAR);
    s.setMontoMulta(new java.math.BigDecimal("50.00"));
    sancionRepository.saveAndFlush(s);
    em.clear();

    assertThat(postCertificado(tokenAdmin, cuerpoCertificadoDirecto(estudiante1)).getResponse().getStatus())
        .isEqualTo(409);
  }

  /**
   * DEF-16 (defecto detectado): las comprobaciones "es encargado de la
   * biblioteca" recorren {@code biblioteca.getEncargados()} sin filtrar por
   * {@code activo} ni por {@code fechaFin}. Un bibliotecario dado de baja de la
   * biblioteca sigue pudiendo generar y anular certificados.
   */
  @Test
  @Disabled("DEF-16: un encargado dado de baja (activo=false) sigue pudiendo generar/anular certificados")
  @DisplayName("IT4-I-CER-12: un encargado inactivo ya no puede generar certificados (409)")
  void encargadoInactivo() throws Exception {
    long solicitud = solicitudAprobada(estudiante1);
    for (BibliotecaEncargado e : encargadoRepository.findAll()) {
      e.setActivo(false);
      e.setFechaFin(LocalDateTime.now().minusDays(1));
      encargadoRepository.saveAndFlush(e);
    }
    em.clear();

    assertThat(postCertificado(tokenBiblio, cuerpoCertificado(solicitud)).getResponse().getStatus())
        .isEqualTo(409);
  }

  /**
   * DEF-12 (parte certificados): {@code CertificadoRequest} no exige
   * {@code razonCertificadoId} en la generación directa; {@code findById(null)}
   * produce una excepción interna (500) en lugar de un 400.
   */
  @Test
  @Disabled("DEF-12: CertificadoRequest sin validar la razón en generación directa -> 500")
  @DisplayName("IT4-I-CER-13: generación directa sin razón de certificado devuelve 400")
  void directoSinRazon() throws Exception {
    Map<String, Object> body = cuerpoCertificadoDirecto(estudiante1);
    body.remove("razonCertificadoId");

    assertThat(postCertificado(tokenAdmin, body).getResponse().getStatus()).isEqualTo(400);
  }

  // ===================== VALIDACIÓN PÚBLICA =====================

  @Test
  @DisplayName("IT4-I-VAL-01: la validación por código es pública y confirma un certificado vigente")
  void validar_vigente() throws Exception {
    long id = certificadoEmitido(estudiante1, null);

    MvcResult r = validar(codigoDe(id));

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(cuerpo(r).path("data").path("valido").asBoolean()).isTrue();
    assertThat(cuerpo(r).path("data").path("certificado").path("id_certificado").asLong()).isEqualTo(id);
  }

  @Test
  @DisplayName("IT4-I-VAL-02: un código inexistente se informa como no válido (sin error)")
  void validar_codigoFalso() throws Exception {
    MvcResult r = validar("CODIGO-INVENTADO");

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(cuerpo(r).path("data").path("valido").asBoolean()).isFalse();
    assertThat(cuerpo(r).path("data").path("mensaje").asText()).contains("no encontrado");
  }

  @Test
  @DisplayName("IT4-I-VAL-03: un certificado ANULADO deja de ser válido")
  void validar_anulado() throws Exception {
    long id = certificadoEmitido(estudiante1, null);
    String codigo = codigoDe(id);
    patchAnular(tokenBiblio, id);

    MvcResult r = validar(codigo);

    assertThat(cuerpo(r).path("data").path("valido").asBoolean()).isFalse();
    assertThat(cuerpo(r).path("data").path("mensaje").asText()).contains("no está vigente");
  }

  @Test
  @DisplayName("IT4-I-VAL-04: un certificado con fecha de vencimiento pasada se informa como vencido")
  void validar_vencidoPorFecha() throws Exception {
    long id = certificadoEmitido(estudiante1, 30);
    forzarVencimiento(id);

    MvcResult r = validar(codigoDe(id));

    assertThat(cuerpo(r).path("data").path("valido").asBoolean()).isFalse();
    assertThat(cuerpo(r).path("data").path("mensaje").asText()).contains("vencido");
  }

  private void forzarVencimiento(long idCertificado) {
    em.flush();
    CertificadoNoDeuda c = certificadoRepository.findById(idCertificado).orElseThrow();
    c.setFechaVencimiento(LocalDateTime.now().minusHours(1));
    certificadoRepository.saveAndFlush(c);
    em.clear();
  }

  // ===================== ANULACIÓN Y VENCIMIENTO =====================

  @Test
  @DisplayName("IT4-I-ANU-01: el bibliotecario encargado anula un certificado VIGENTE")
  void anular_ok() throws Exception {
    long id = certificadoEmitido(estudiante1, null);

    MvcResult r = patchAnular(tokenBiblio, id);

    assertThat(r.getResponse().getStatus()).isEqualTo(200);
    assertThat(cuerpo(r).path("data").path("estadoCertificado").asText()).isEqualTo("ANULADO");
  }

  @Test
  @DisplayName("IT4-I-ANU-02: anular un certificado ya anulado -> 409")
  void anular_doble() throws Exception {
    long id = certificadoEmitido(estudiante1, null);
    patchAnular(tokenBiblio, id);

    assertThat(patchAnular(tokenBiblio, id).getResponse().getStatus()).isEqualTo(409);
  }

  @Test
  @DisplayName("IT4-I-ANU-03: un ESTUDIANTE no puede anular (403) y un bibliotecario ajeno tampoco (409)")
  void anular_permisos() throws Exception {
    long id = certificadoEmitido(estudiante1, null);

    assertThat(patchAnular(tokenEstudiante1, id).getResponse().getStatus()).isEqualTo(403);
    assertThat(patchAnular(tokenOtroBiblio, id).getResponse().getStatus()).isEqualTo(409);
    assertThat(certificadoRepository.findById(id).orElseThrow().getEstadoCertificado())
        .isEqualTo(EstadoCertificado.VIGENTE);
  }

  @Test
  @DisplayName("IT4-I-ANU-04: anular un certificado inexistente -> 404")
  void anular_inexistente() throws Exception {
    assertThat(patchAnular(tokenAdmin, 999999L).getResponse().getStatus()).isEqualTo(404);
  }

  @Test
  @DisplayName("IT4-I-VEN-01: la tarea de vencimiento pasa a VENCIDO los certificados con fecha pasada")
  void tareaVencidos() throws Exception {
    long vencido = certificadoEmitido(estudiante1, 30);
    long vigente = certificadoEmitido(estudiante2, 30);
    forzarVencimiento(vencido);

    certificadoService.actualizarCertificadosVencidos();

    em.flush();
    em.clear();
    assertThat(certificadoRepository.findById(vencido).orElseThrow().getEstadoCertificado())
        .isEqualTo(EstadoCertificado.VENCIDO);
    assertThat(certificadoRepository.findById(vigente).orElseThrow().getEstadoCertificado())
        .isEqualTo(EstadoCertificado.VIGENTE);
  }

  // ===================== CONSULTAS Y ACCESO =====================

  @Test
  @DisplayName("IT4-I-QRY-01: el estudiante ve sus certificados; los de otro estudiante le dan 409")
  void consulta_porUsuario() throws Exception {
    certificadoEmitido(estudiante1, null);

    mockMvc.perform(get("/api/certificados/usuario/" + estudiante1.getId_usuario())
        .header("Authorization", tokenEstudiante1))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
    mockMvc.perform(get("/api/certificados/usuario/" + estudiante1.getId_usuario())
        .header("Authorization", tokenEstudiante2))
        .andExpect(status().isConflict());
    mockMvc.perform(get("/api/certificados/usuario/" + estudiante1.getId_usuario())
        .header("Authorization", tokenBiblio))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
  }

  @Test
  @DisplayName("IT4-I-QRY-02: listar por biblioteca con filtro de estado (solo personal a cargo)")
  void consulta_porBiblioteca() throws Exception {
    long anulado = certificadoEmitido(estudiante1, null);
    certificadoEmitido(estudiante2, null);
    patchAnular(tokenBiblio, anulado);

    mockMvc.perform(get("/api/certificados/biblioteca/" + idBiblioteca).header("Authorization", tokenBiblio))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2));
    mockMvc.perform(get("/api/certificados/biblioteca/" + idBiblioteca).param("estado", "VIGENTE")
        .header("Authorization", tokenBiblio))
        .andExpect(jsonPath("$.data.length()").value(1));
    mockMvc.perform(get("/api/certificados/biblioteca/" + idBiblioteca).header("Authorization", tokenEstudiante1))
        .andExpect(status().isForbidden());
    mockMvc.perform(get("/api/certificados/biblioteca/" + idBiblioteca).header("Authorization", tokenOtroBiblio))
        .andExpect(status().isConflict());
    mockMvc.perform(get("/api/certificados/biblioteca/" + idBiblioteca).header("Authorization", tokenAdmin))
        .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2));
  }

  @Test
  @DisplayName("IT4-I-QRY-03: sin sesión no se consultan certificados")
  void consulta_sinToken() throws Exception {
    mockMvc.perform(get("/api/certificados/usuario/" + estudiante1.getId_usuario()))
        .andExpect(status().is4xxClientError());
    mockMvc.perform(get("/api/certificados/biblioteca/" + idBiblioteca)).andExpect(status().is4xxClientError());
  }
}
