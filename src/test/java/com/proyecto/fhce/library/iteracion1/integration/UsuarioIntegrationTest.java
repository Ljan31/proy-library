package com.proyecto.fhce.library.iteracion1.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.proyecto.fhce.library.dto.request.users.ChangePasswordRequest;
import com.proyecto.fhce.library.dto.request.users.LoginRequest;
import com.proyecto.fhce.library.dto.request.users.PersonaRequest;
import com.proyecto.fhce.library.dto.request.users.RegisterRequest;
import com.proyecto.fhce.library.dto.request.users.RegisterRequestEst;
import com.proyecto.fhce.library.dto.request.users.UsuarioCarreraRequestEst;
import com.proyecto.fhce.library.dto.request.users.UsuarioUpdateRequest;
import com.proyecto.fhce.library.entities.Carrera;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.CarreraRepository;
import com.proyecto.fhce.library.support.IntegrationTestBase;

/**
 * Iteración 1 · Integración de gestión de usuarios (registro, autorización por
 * rol,
 * contraseñas, habilitar/deshabilitar y eliminación).
 */
@DisplayName("Iteración 1 · Gestión de usuarios (integración)")
class UsuarioIntegrationTest extends IntegrationTestBase {

  @Autowired
  CarreraRepository carreraRepository;

  private Usuario admin;
  private Usuario bibliotecario;
  private Usuario estudiante;
  private Usuario otroEstudiante;

  @BeforeEach
  void datos() {
    admin = crearUsuario("admin01", rolAdmin);
    bibliotecario = crearUsuario("biblio01", rolBibliotecario);
    estudiante = crearUsuario("estud01", rolEstudiante);
    otroEstudiante = crearUsuario("estud02", rolEstudiante);
    flushYLimpiar();
  }

  private PersonaRequest personaReq(String ci, String email) {
    PersonaRequest p = new PersonaRequest();
    p.setNombre("Ana");
    p.setApellido_pat("Lopez");
    p.setApellido_mat("Rojas");
    p.setCi(ci);
    p.setCelular("71234567");
    p.setEmail(email);
    return p;
  }

  private RegisterRequest registro(String username, String ci, String email, Set<Long> roleIds) {
    RegisterRequest r = new RegisterRequest();
    r.setUsername(username);
    r.setPassword("secreto123");
    r.setPersona(personaReq(ci, email));
    r.setRoleIds(roleIds);
    return r;
  }

  private int statusLogin(String user, String pass) throws Exception {
    LoginRequest l = new LoginRequest();
    l.setUsername(user);
    l.setPassword(pass);
    return mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(json(l)))
        .andReturn().getResponse().getStatus();
  }

  // ===================== REGISTRO =====================

  @Test
  @DisplayName("IT1-I-USR-01: el ADMIN registra un usuario con rol; se guarda con contraseña encriptada y puede iniciar sesión")
  void admin_registraUsuario_ok() throws Exception {
    RegisterRequest req = registro("nuevo01", "5550001", "nuevo01@correo.com",
        Set.of(rolBibliotecario.getId_role()));

    mockMvc.perform(post("/api/auth/register").header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.username").value("nuevo01"))
        .andExpect(jsonPath("$.data.roles[0].name").value("ROLE_BIBLIOTECARIO"))
        .andExpect(jsonPath("$.data.persona.ci").value("5550001"));

    flushYLimpiar();
    Usuario guardado = userRepository.findByUsername("nuevo01").orElseThrow();
    assertThat(guardado.getPassword()).isNotEqualTo("secreto123").startsWith("$2"); // BCrypt
    assertThat(statusLogin("nuevo01", "secreto123")).isEqualTo(200);
  }

  @Test
  @DisplayName("IT1-I-USR-02: el BIBLIOTECARIO también puede registrar usuarios")
  void bibliotecario_registraUsuario_ok() throws Exception {
    RegisterRequest req = registro("nuevo02", "5550002", "nuevo02@correo.com", null);

    mockMvc.perform(post("/api/auth/register").header("Authorization", bearer("biblio01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.roles[0].name").value("ROLE_ESTUDIANTE")); // rol por defecto
  }

  @Test
  @DisplayName("IT1-I-USR-03: un ESTUDIANTE no puede registrar usuarios (403)")
  void estudiante_noPuedeRegistrar() throws Exception {
    RegisterRequest req = registro("nuevo03", "5550003", "nuevo03@correo.com", null);

    mockMvc.perform(post("/api/auth/register").header("Authorization", bearer("estud01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isForbidden());
    assertThat(userRepository.existsByUsername("nuevo03")).isFalse();
  }

  @Test
  @DisplayName("IT1-I-USR-04: registrar un username repetido devuelve 409")
  void registrar_usernameRepetido() throws Exception {
    RegisterRequest req = registro("estud01", "5550004", "x4@correo.com", null);

    mockMvc.perform(post("/api/auth/register").header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Username ya existe: estud01"));
  }

  @Test
  @DisplayName("IT1-I-USR-05: registrar una persona con CI repetido devuelve 409")
  void registrar_ciRepetido() throws Exception {
    String ciExistente = userRepository.findByUsername("estud01").orElseThrow().getPersona().getCi();
    RegisterRequest req = registro("nuevo05", ciExistente, "nuevo05@correo.com", null);

    mockMvc.perform(post("/api/auth/register").header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Ya existe una persona con CI: " + ciExistente));
  }

  @Test
  @DisplayName("IT1-I-USR-06: registrar con contraseña corta devuelve 400 con el mensaje de validación")
  void registrar_passwordCorta() throws Exception {
    RegisterRequest req = registro("nuevo06", "5550006", "nuevo06@correo.com", null);
    req.setPassword("123");

    mockMvc.perform(post("/api/auth/register").header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("password: Password debe tener al menos 6 caracteres"));
  }

  /**
   * DEFECTO DETECTADO (DEF-03): RegisterRequest.persona no lleva @Valid, así que
   * las
   * restricciones de PersonaRequest (celular de 7-8 dígitos, email válido, CI >
   * 0)
   * NO se validan al registrar usuarios (tampoco en /api/users/estudiante, que
   * además
   * no tiene @Valid en el controlador). Agregar @Valid al campo y
   * al @RequestBody.
   */
  @Test
  // @Disabled("DEF-03: falta @Valid en RegisterRequest.persona. Habilitar tras
  // corregir.")
  @DisplayName("IT1-I-USR-07: registrar con celular inválido debe devolver 400")
  void registrar_celularInvalido() throws Exception {
    RegisterRequest req = registro("nuevo07", "5550007", "nuevo07@correo.com", null);
    req.getPersona().setCelular("12345");

    mockMvc.perform(post("/api/auth/register").header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT1-I-USR-08: el registro PÚBLICO de estudiante no requiere token y asigna ROLE_ESTUDIANTE")
  void registroPublicoEstudiante_ok() throws Exception {
    RegisterRequestEst req = new RegisterRequestEst();
    req.setUsername("publico01");
    req.setPassword("secreto123");
    req.setPersona(personaReq("5550008", "publico01@correo.com"));

    mockMvc.perform(post("/api/users/estudiante").contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.username").value("publico01"))
        .andExpect(jsonPath("$.data.roles[0].name").value("ROLE_ESTUDIANTE"));
    assertThat(statusLogin("publico01", "secreto123")).isEqualTo(200);
  }

  @Test
  @DisplayName("IT1-I-USR-09: el registro público de estudiante puede asociar una carrera con su matrícula")
  void registroPublicoEstudiante_conCarrera() throws Exception {
    Carrera carrera = new Carrera();
    carrera.setNombre_carrera("Historia");
    carrera.setCodigoCarrera("HIST");
    carrera = carreraRepository.saveAndFlush(carrera);

    UsuarioCarreraRequestEst uc = new UsuarioCarreraRequestEst();
    uc.setCarreraId(carrera.getIdCarrera());
    uc.setMatricula("2024-0001");
    RegisterRequestEst req = new RegisterRequestEst();
    req.setUsername("publico02");
    req.setPassword("secreto123");
    req.setPersona(personaReq("5550009", "publico02@correo.com"));
    req.setUserCarreras(Set.of(uc));

    mockMvc.perform(post("/api/users/estudiante").contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.userCarrera[0].matricula").value("2024-0001"));
  }

  // ===================== CONSULTAS Y AUTORIZACIÓN POR ROL =====================

  @Test
  @DisplayName("IT1-I-USR-10: el ADMIN lista todos los usuarios")
  void admin_listaUsuarios() throws Exception {
    mockMvc.perform(get("/api/users").header("Authorization", bearer("admin01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(4))
        .andExpect(jsonPath("$.data[?(@.username=='estud01')]").exists());
  }

  @Test
  @DisplayName("IT1-I-USR-11: un ESTUDIANTE no puede listar usuarios (403)")
  void estudiante_noListaUsuarios() throws Exception {
    mockMvc.perform(get("/api/users").header("Authorization", bearer("estud01")))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT1-I-USR-12: un ESTUDIANTE puede ver su propio usuario pero no el de otro")
  void estudiante_soloVeSuPropioUsuario() throws Exception {
    String token = bearer("estud01");

    mockMvc.perform(get("/api/users/" + estudiante.getId_usuario()).header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.username").value("estud01"));

    mockMvc.perform(get("/api/users/" + otroEstudiante.getId_usuario()).header("Authorization", token))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT1-I-USR-13: consultar un usuario inexistente devuelve 404")
  void findById_noExiste() throws Exception {
    mockMvc.perform(get("/api/users/999999").header("Authorization", bearer("admin01")))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Usuario no encontrado con id: 999999"));
  }

  @Test
  @DisplayName("IT1-I-USR-14: filtrar usuarios por rol (con y sin el prefijo ROLE_)")
  void findByRole() throws Exception {
    mockMvc.perform(get("/api/users/by-role").param("roleName", "ESTUDIANTE")
        .header("Authorization", bearer("admin01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2));

    mockMvc.perform(get("/api/users/by-role").param("roleName", "ROLE_BIBLIOTECARIO")
        .header("Authorization", bearer("admin01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data[0].username").value("biblio01"));
  }

  @Test
  @DisplayName("IT1-I-USR-15: la búsqueda de usuarios no distingue mayúsculas/minúsculas")
  void search_ignoraMayusculas() throws Exception {
    mockMvc.perform(get("/api/users/search").param("q", "ESTUD01").header("Authorization", bearer("admin01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].username").value("estud01"));
  }

  // ===================== ACTUALIZACIÓN =====================

  @Test
  @DisplayName("IT1-I-USR-16: el ADMIN actualiza datos de un usuario y el cambio queda persistido")
  void admin_actualizaUsuario() throws Exception {
    UsuarioUpdateRequest req = new UsuarioUpdateRequest();
    req.setNombre("Cambiado");
    req.setCelular("78888888");

    mockMvc.perform(put("/api/users/" + estudiante.getId_usuario()).header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.persona.nombre").value("Cambiado"));

    flushYLimpiar();
    assertThat(userRepository.findByUsername("estud01").orElseThrow().getPersona().getCelular())
        .isEqualTo("78888888");
  }

  @Test
  @DisplayName("IT1-I-USR-17: un ESTUDIANTE puede editar su propio usuario pero no el de otro")
  void estudiante_soloEditaSuPropioUsuario() throws Exception {
    UsuarioUpdateRequest req = new UsuarioUpdateRequest();
    req.setNombre("MiNombre");
    String token = bearer("estud01");

    mockMvc.perform(put("/api/users/" + estudiante.getId_usuario()).header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isOk());

    mockMvc.perform(put("/api/users/" + otroEstudiante.getId_usuario()).header("Authorization", token)
        .contentType(MediaType.APPLICATION_JSON).content(json(req)))
        .andExpect(status().isForbidden());
  }

  // ===================== CONTRASEÑAS =====================

  private ChangePasswordRequest cambio(String actual, String nueva, String confirma) {
    ChangePasswordRequest c = new ChangePasswordRequest();
    c.setCurrentPassword(actual);
    c.setNewPassword(nueva);
    c.setConfirmPassword(confirma);
    return c;
  }

  @Test
  @DisplayName("IT1-I-USR-18: cambiar contraseña: la nueva sirve para iniciar sesión y la anterior ya no")
  void cambiarPassword_ok() throws Exception {
    mockMvc.perform(put("/api/users/change-password").header("Authorization", bearer("estud01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(cambio(PASSWORD, "NuevaClave9", "NuevaClave9"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Password actualizado"));

    assertThat(statusLogin("estud01", "NuevaClave9")).isEqualTo(200);
    assertThat(statusLogin("estud01", PASSWORD)).isEqualTo(401);
  }

  @Test
  @DisplayName("IT1-I-USR-19: cambiar contraseña con la actual incorrecta devuelve 400")
  void cambiarPassword_actualIncorrecta() throws Exception {
    mockMvc.perform(put("/api/users/change-password").header("Authorization", bearer("estud01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(cambio("incorrecta", "NuevaClave9", "NuevaClave9"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Password actual incorrecto"));
  }

  @Test
  @DisplayName("IT1-I-USR-20: cambiar contraseña con confirmación distinta devuelve 400")
  void cambiarPassword_confirmacionDistinta() throws Exception {
    mockMvc.perform(put("/api/users/change-password").header("Authorization", bearer("estud01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(cambio(PASSWORD, "NuevaClave9", "Otra123456"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Los passwords no coinciden"));
  }

  @Test
  @DisplayName("IT1-I-USR-21: el ADMIN restablece la contraseña de un estudiante y la temporal permite iniciar sesión")
  void adminResetPassword_ok() throws Exception {
    MvcResult result = mockMvc.perform(put("/api/users/" + estudiante.getId_usuario() + "/reset-password")
        .header("Authorization", bearer("admin01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.temporaryPassword").isNotEmpty())
        .andReturn();
    String temporal = cuerpo(result).path("data").path("temporaryPassword").asText();

    assertThat(temporal).hasSize(8);
    assertThat(statusLogin("estud01", temporal)).isEqualTo(200);
    assertThat(statusLogin("estud01", PASSWORD)).isEqualTo(401);
  }

  @Test
  @DisplayName("IT1-I-USR-22: no se puede restablecer la contraseña de un ADMIN (400)")
  void adminResetPassword_aAdmin() throws Exception {
    Usuario otroAdmin = crearUsuario("admin02", rolAdmin);
    flushYLimpiar();

    mockMvc.perform(put("/api/users/" + otroAdmin.getId_usuario() + "/reset-password")
        .header("Authorization", bearer("admin01")))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT1-I-USR-23: un ESTUDIANTE no puede restablecer contraseñas (403)")
  void estudiante_noResetea() throws Exception {
    mockMvc.perform(put("/api/users/" + otroEstudiante.getId_usuario() + "/reset-password")
        .header("Authorization", bearer("estud01")))
        .andExpect(status().isForbidden());
  }

  // ===================== HABILITAR / ELIMINAR =====================

  @Test
  @DisplayName("IT1-I-USR-24: el ADMIN deshabilita a un usuario y éste ya no puede iniciar sesión")
  void toggleEnabled_deshabilita() throws Exception {
    mockMvc.perform(put("/api/users/" + estudiante.getId_usuario() + "/toggle-enabled")
        .header("Authorization", bearer("admin01")))
        .andExpect(status().isOk());

    assertThat(statusLogin("estud01", PASSWORD)).isEqualTo(403);
  }

  @Test
  @DisplayName("IT1-I-USR-25: solo el ADMIN puede habilitar/deshabilitar usuarios")
  void toggleEnabled_soloAdmin() throws Exception {
    mockMvc.perform(put("/api/users/" + estudiante.getId_usuario() + "/toggle-enabled")
        .header("Authorization", bearer("biblio01")))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT1-I-USR-26: el ADMIN elimina un usuario sin dependencias y éste desaparece de la BD")
  void delete_ok() throws Exception {
    mockMvc.perform(delete("/api/users/" + otroEstudiante.getId_usuario())
        .header("Authorization", bearer("admin01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Usuario eliminado correctamente"));

    flushYLimpiar();
    assertThat(userRepository.findByUsername("estud02")).isEmpty();
  }

  @Test
  @DisplayName("IT1-I-USR-27: un ESTUDIANTE no puede eliminar usuarios (403)")
  void delete_estudianteNoPuede() throws Exception {
    mockMvc.perform(delete("/api/users/" + otroEstudiante.getId_usuario())
        .header("Authorization", bearer("estud01")))
        .andExpect(status().isForbidden());
  }
}
