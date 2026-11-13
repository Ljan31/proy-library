package com.proyecto.fhce.library.iteracion1.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.proyecto.fhce.library.dto.request.users.RoleRequest;
import com.proyecto.fhce.library.entities.Permiso;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.PermisoRepository;
import com.proyecto.fhce.library.support.IntegrationTestBase;

/**
 * Iteración 1 · Integración de roles (tabla roles / users_roles) y acceso a
 * personas.
 */
@DisplayName("Iteración 1 · Roles y personas (integración)")
class RolesYPersonasIntegrationTest extends IntegrationTestBase {

  @Autowired
  PermisoRepository permisoRepository;

  private Usuario admin;
  private Usuario estudiante;
  private Usuario otroEstudiante;

  @BeforeEach
  void datos() {
    admin = crearUsuario("admin01", rolAdmin);
    estudiante = crearUsuario("estud01", rolEstudiante);
    otroEstudiante = crearUsuario("estud02", rolEstudiante);
    flushYLimpiar();
  }

  private RoleRequest roleReq(String nombre) {
    RoleRequest r = new RoleRequest();
    r.setName(nombre);
    return r;
  }

  // ===================== ROLES =====================

  @Test
  @DisplayName("IT1-I-ROL-01: el ADMIN lista los roles del sistema")
  void admin_listaRoles() throws Exception {
    mockMvc.perform(get("/api/roles").header("Authorization", bearer("admin01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(4));
  }

  @Test
  @DisplayName("IT1-I-ROL-02: el ADMIN crea un rol nuevo y queda guardado en la BD")
  void admin_creaRol() throws Exception {
    mockMvc.perform(post("/api/roles").header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(roleReq("ROLE_TEMPORAL"))))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.data.name").value("ROLE_TEMPORAL"));

    assertThat(roleRepository.existsByName("ROLE_TEMPORAL")).isTrue();
  }

  @Test
  @DisplayName("IT1-I-ROL-03: crear un rol con nombre repetido devuelve 409")
  void crearRol_duplicado() throws Exception {
    mockMvc.perform(post("/api/roles").header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(roleReq("ROLE_ADMIN"))))
        .andExpect(status().isConflict());
  }

  @Test
  @DisplayName("IT1-I-ROL-04: crear un rol sin nombre devuelve 400")
  void crearRol_sinNombre() throws Exception {
    mockMvc.perform(post("/api/roles").header("Authorization", bearer("admin01"))
        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("IT1-I-ROL-05: un ESTUDIANTE no puede crear roles (403)")
  void estudiante_noCreaRoles() throws Exception {
    mockMvc.perform(post("/api/roles").header("Authorization", bearer("estud01"))
        .contentType(MediaType.APPLICATION_JSON).content(json(roleReq("ROLE_HACK"))))
        .andExpect(status().isForbidden());
    assertThat(roleRepository.existsByName("ROLE_HACK")).isFalse();
  }

  @Test
  @DisplayName("IT1-I-ROL-06: no se puede eliminar un rol que tiene usuarios asignados (409)")
  void eliminarRol_conUsuarios() throws Exception {
    mockMvc.perform(delete("/api/roles/" + rolEstudiante.getId_role()).header("Authorization", bearer("admin01")))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("usuarios asignados")));
  }

  @Test
  @DisplayName("IT1-I-ROL-07: se puede eliminar un rol sin usuarios")
  void eliminarRol_sinUsuarios() throws Exception {
    mockMvc.perform(delete("/api/roles/" + rolAuxiliar.getId_role()).header("Authorization", bearer("admin01")))
        .andExpect(status().isOk());

    flushYLimpiar();
    assertThat(roleRepository.existsByName("ROLE_AUXILIAR")).isFalse();
  }

  /**
   * DEFECTO DETECTADO (DEF-02): en RoleController los endpoints
   * POST/DELETE /api/roles/{roleId}/permisos/{permisoId} NO tienen @PreAuthorize,
   * por lo que cualquier usuario autenticado (incluso un ESTUDIANTE) puede
   * modificar los permisos de un rol (escalada de privilegios).
   * Agregar @PreAuthorize("hasRole('ADMIN')") y quitar @Disabled.
   */
  @Test
  // @Disabled("DEF-02: faltan @PreAuthorize en asignar/remover permisos de un
  // rol. Habilitar tras corregir.")
  @DisplayName("IT1-I-ROL-08: un ESTUDIANTE no debe poder asignar permisos a un rol (403)")
  void estudiante_noAsignaPermisos() throws Exception {
    Permiso p = new Permiso();
    p.setNombre_permiso("LIBROS_VER");
    p.setModulo("LIBROS");
    p = permisoRepository.saveAndFlush(p);

    mockMvc.perform(post("/api/roles/" + rolEstudiante.getId_role() + "/permisos/" + p.getId_permiso())
        .header("Authorization", bearer("estud01")))
        .andExpect(status().isForbidden());
  }

  // ===================== PERSONAS =====================

  @Test
  @DisplayName("IT1-I-PER-01: un usuario puede consultar su propia persona")
  void persona_propia_ok() throws Exception {
    Long idPersona = userRepository.findByUsername("estud01").orElseThrow().getPersona().getId_persona();

    mockMvc.perform(get("/api/personas/" + idPersona).header("Authorization", bearer("estud01")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.id_persona").value(idPersona));
  }

  @Test
  @DisplayName("IT1-I-PER-02: un ESTUDIANTE no puede consultar la persona de otro usuario (403)")
  void persona_ajena_denegada() throws Exception {
    Long idAjena = userRepository.findByUsername("estud02").orElseThrow().getPersona().getId_persona();

    mockMvc.perform(get("/api/personas/" + idAjena).header("Authorization", bearer("estud01")))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("IT1-I-PER-03: el ADMIN puede consultar cualquier persona")
  void persona_admin_ok() throws Exception {
    Long idAjena = userRepository.findByUsername("estud02").orElseThrow().getPersona().getId_persona();

    mockMvc.perform(get("/api/personas/" + idAjena).header("Authorization", bearer("admin01")))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("IT1-I-PER-04: consultar una persona inexistente devuelve 404")
  void persona_inexistente() throws Exception {
    mockMvc.perform(get("/api/personas/999999").header("Authorization", bearer("admin01")))
        .andExpect(status().isNotFound());
  }
}
