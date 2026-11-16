package com.proyecto.fhce.library.iteracion4.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.proyecto.fhce.library.entities.RazonCertificado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.iteracion3.integration.PrestamoIntegrationBase;
import com.proyecto.fhce.library.repositories.CertificadoNoDeudaRepository;
import com.proyecto.fhce.library.repositories.RazonCertificadoRepository;
import com.proyecto.fhce.library.repositories.SolicitudCertificadoRepository;

/**
 * Escenario común de la iteración 4. Reutiliza el de la iteración 3
 * (biblioteca con bibliotecario encargado, 2 estudiantes, configuración de
 * préstamos y catálogo) y agrega un administrador y una razón de certificado.
 */
public abstract class CertificadoIntegrationBase extends PrestamoIntegrationBase {

  @Autowired
  protected RazonCertificadoRepository razonRepository;
  @Autowired
  protected SolicitudCertificadoRepository solicitudRepository;
  @Autowired
  protected CertificadoNoDeudaRepository certificadoRepository;

  protected String tokenAdmin;
  protected Long idRazon;

  @BeforeEach
  void escenarioCertificados() throws Exception {
    crearUsuario("admin01", rolAdmin);
    RazonCertificado r = new RazonCertificado();
    r.setBiblioteca(bibliotecaRepository.findById(idBiblioteca).orElseThrow());
    r.setNombre("Titulación");
    r.setDescripcion("Certificado para titulación");
    r.setRequisitos("[\"CI\"]");
    r.setActivo(true);
    idRazon = razonRepository.saveAndFlush(r).getIdRazon();
    flushYLimpiar();
    tokenAdmin = bearer("admin01");
  }

  // ---------------- helpers ----------------

  protected Map<String, Object> cuerpoSolicitud(Usuario titular) {
    Map<String, Object> m = new HashMap<>();
    m.put("bibliotecaId", idBiblioteca);
    m.put("razonCertificadoId", idRazon);
    m.put("nombres", titular.getPersona().getNombre());
    m.put("apellidos", titular.getPersona().getApellido_pat() + " " + titular.getPersona().getApellido_mat());
    m.put("ci", titular.getPersona().getCi());
    m.put("matricula", "2020-" + titular.getId_usuario());
    m.put("email", titular.getPersona().getEmail());
    m.put("telefono", "71234567");
    m.put("descripcion", "Para titulación");
    return m;
  }

  /** token puede ser null (solicitud pública, sin sesión). */
  protected MvcResult postSolicitud(String token, Map<String, Object> cuerpo) throws Exception {
    MockHttpServletRequestBuilder b = post("/api/solicitudes-certificado")
        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpo));
    if (token != null) {
      b = b.header("Authorization", token);
    }
    return mockMvc.perform(b).andReturn();
  }

  protected long crearSolicitud(Usuario titular) throws Exception {
    MvcResult r = postSolicitud(null, cuerpoSolicitud(titular));
    assertThat(r.getResponse().getStatus()).as(r.getResponse().getContentAsString()).isEqualTo(201);
    return cuerpo(r).path("data").path("id").asLong();
  }

  protected MvcResult resolverSolicitud(String token, long id, String accion) throws Exception {
    return mockMvc.perform(patch("/api/solicitudes-certificado/" + id + "/" + accion)
        .param("observacion", "Revisado").header("Authorization", token)).andReturn();
  }

  protected long solicitudAprobada(Usuario titular) throws Exception {
    long id = crearSolicitud(titular);
    assertThat(resolverSolicitud(tokenBiblio, id, "aprobar").getResponse().getStatus()).isEqualTo(200);
    return id;
  }

  protected MvcResult postCertificado(String token, Map<String, Object> cuerpo) throws Exception {
    return mockMvc.perform(post("/api/certificados").header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON).content(json(cuerpo))).andReturn();
  }

  protected Map<String, Object> cuerpoCertificado(long solicitudId) {
    Map<String, Object> m = new HashMap<>();
    m.put("bibliotecaId", idBiblioteca);
    m.put("solicitudId", solicitudId);
    return m;
  }

  protected Map<String, Object> cuerpoCertificadoDirecto(Usuario titular) {
    Map<String, Object> m = new HashMap<>();
    m.put("bibliotecaId", idBiblioteca);
    m.put("usuarioId", titular.getId_usuario());
    m.put("razonCertificadoId", idRazon);
    return m;
  }

  /** Flujo completo: solicitud -> aprobación -> certificado. Devuelve el id del certificado. */
  protected long certificadoEmitido(Usuario titular, Integer diasValidez) throws Exception {
    long sol = solicitudAprobada(titular);
    Map<String, Object> body = cuerpoCertificado(sol);
    if (diasValidez != null) {
      body.put("diasValidez", diasValidez);
    }
    MvcResult r = postCertificado(tokenBiblio, body);
    assertThat(r.getResponse().getStatus()).as(r.getResponse().getContentAsString()).isEqualTo(201);
    return cuerpo(r).path("data").path("id_certificado").asLong();
  }
}
