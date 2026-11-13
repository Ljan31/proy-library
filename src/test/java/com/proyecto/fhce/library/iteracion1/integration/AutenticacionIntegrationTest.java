package com.proyecto.fhce.library.iteracion1.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.proyecto.fhce.library.dto.request.users.LoginRequest;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.support.IntegrationTestBase;

/**
 * Iteración 1 · Integración de autenticación (login, JWT, filtro de seguridad).
 * Recorre: HTTP -> filtro JWT -> controlador -> servicio -> repositorio -> BD H2.
 */
@DisplayName("Iteración 1 · Autenticación (integración)")
class AutenticacionIntegrationTest extends IntegrationTestBase {

  private Usuario admin;
  private Usuario estudiante;

  @BeforeEach
  void datos() {
    admin = crearUsuario("admin01", rolAdmin);
    estudiante = crearUsuario("estud01", rolEstudiante);
    flushYLimpiar();
  }

  private LoginRequest login(String user, String pass) {
    LoginRequest r = new LoginRequest();
    r.setUsername(user);
    r.setPassword(pass);
    return r;
  }

  @Test
  @DisplayName("IT1-I-AUT-01: login con credenciales correctas devuelve 200, token JWT, roles y datos de la persona")
  void login_ok() throws Exception {
    mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content(json(login("admin01", PASSWORD))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.message").value("Login exitoso"))
        .andExpect(jsonPath("$.data.token").isNotEmpty())
        .andExpect(jsonPath("$.data.username").value("admin01"))
        .andExpect(jsonPath("$.data.roles[0]").value("ROLE_ADMIN"))
        .andExpect(jsonPath("$.data.persona.ci").isNotEmpty());
  }

  @Test
  @DisplayName("IT1-I-AUT-02: login con contraseña incorrecta devuelve 401 'Credenciales inválidas'")
  void login_passwordIncorrecta() throws Exception {
    mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content(json(login("admin01", "incorrecta"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
  }

  @Test
  @DisplayName("IT1-I-AUT-03: login con usuario inexistente devuelve 401 (no revela si el usuario existe)")
  void login_usuarioInexistente() throws Exception {
    mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content(json(login("fantasma", PASSWORD))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Credenciales inválidas"));
  }

  @Test
  @DisplayName("IT1-I-AUT-04: login de un usuario deshabilitado devuelve 403")
  void login_usuarioDeshabilitado() throws Exception {
    Usuario u = userRepository.findByUsername("estud01").orElseThrow();
    u.setEnabled(false);
    userRepository.saveAndFlush(u);

    mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content(json(login("estud01", PASSWORD))))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("El usuario está deshabilitado"));
  }

  @Test
  @DisplayName("IT1-I-AUT-05: login con campos vacíos devuelve 400 por validación")
  void login_camposVacios() throws Exception {
    mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content("{\"username\":\"\",\"password\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.success").value(false));
  }

  @Test
  @DisplayName("IT1-I-AUT-06: /api/auth/me con token válido devuelve el usuario autenticado")
  void me_conTokenValido() throws Exception {
    mockMvc.perform(get("/api/auth/me").header("Authorization", bearer("estud01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.username").value("estud01"))
        .andExpect(jsonPath("$.data.roles[0].name").value("ROLE_ESTUDIANTE"));
  }

  @Test
  @DisplayName("IT1-I-AUT-07: un endpoint protegido sin token devuelve 401 con mensaje JSON")
  void endpointProtegido_sinToken() throws Exception {
    mockMvc.perform(get("/api/users"))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.success").value(false))
        .andExpect(jsonPath("$.message").value("Token inválido o expirado"));
  }

  @Test
  @DisplayName("IT1-I-AUT-08: un token inválido devuelve 401")
  void endpointProtegido_tokenInvalido() throws Exception {
    mockMvc.perform(get("/api/users").header("Authorization", "Bearer esto.no.es.un.jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("IT1-I-AUT-09: un token válido deja de servir si el usuario es deshabilitado después")
  void token_deUsuarioDeshabilitadoPosteriormente() throws Exception {
    String bearerAdmin = bearer("admin01");
    mockMvc.perform(get("/api/users").header("Authorization", bearerAdmin)).andExpect(status().isOk());

    Usuario u = userRepository.findByUsername("admin01").orElseThrow();
    u.setEnabled(false);
    userRepository.saveAndFlush(u);

    mockMvc.perform(get("/api/users").header("Authorization", bearerAdmin))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("IT1-I-AUT-10: logout con sesión válida responde 200")
  void logout_ok() throws Exception {
    mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer("estud01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Logout exitoso"));
  }

  @Test
  @DisplayName("IT1-I-AUT-11: el token emitido en el login es aceptado para consumir endpoints según el rol")
  void tokenDeLogin_sirveParaEndpointsAutorizados() throws Exception {
    MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
        .content(json(login("admin01", PASSWORD)))).andReturn();
    String token = cuerpo(login).path("data").path("token").asText();
    assertThat(token.split("\\.")).hasSize(3); // header.payload.firma

    mockMvc.perform(get("/api/users").header("Authorization", "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true));
  }
}
