package com.proyecto.fhce.library.support;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.fhce.library.dto.request.users.LoginRequest;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

import jakarta.persistence.EntityManager;

/**
 * Base de las pruebas de INTEGRACIÓN.
 * - Levanta toda la aplicación (controladores + seguridad JWT + servicios +
 * JPA).
 * - BD H2 en memoria (perfil "test"), esquema creado por Hibernate.
 * - Cada prueba corre dentro de una transacción que se revierte al terminar,
 * así que las pruebas no se contaminan entre sí.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class IntegrationTestBase {

  protected static final String PASSWORD = "Clave1234";
  private static final AtomicLong SECUENCIA = new AtomicLong(1000);

  @Autowired
  protected MockMvc mockMvc;
  @Autowired
  protected ObjectMapper objectMapper;
  @Autowired
  protected EntityManager em;
  @Autowired
  protected PasswordEncoder passwordEncoder;
  @Autowired
  protected RoleRepository roleRepository;
  @Autowired
  protected UserRepository userRepository;

  protected Role rolAdmin;
  protected Role rolBibliotecario;
  protected Role rolAuxiliar;
  protected Role rolEstudiante;

  @BeforeEach
  void crearRolesBase() {
    rolAdmin = roleRepository.save(new Role("ROLE_ADMIN"));
    rolBibliotecario = roleRepository.save(new Role("ROLE_BIBLIOTECARIO"));
    rolAuxiliar = roleRepository.save(new Role("ROLE_AUXILIAR"));
    rolEstudiante = roleRepository.save(new Role("ROLE_ESTUDIANTE"));
    roleRepository.flush();
  }

  // ---------------- datos ----------------

  /**
   * Crea y persiste un usuario habilitado con contraseña PASSWORD (encriptada con
   * BCrypt).
   */
  protected Usuario crearUsuario(String username, Role... roles) {
    long n = SECUENCIA.incrementAndGet();
    Persona p = new Persona();
    p.setNombre("Nombre" + n);
    p.setApellido_pat("Apellido" + n);
    p.setCi(String.valueOf(n * 1000 + 7)); // único
    p.setEmail(username + "@correo.com");
    p.setCelular("71234567");

    Usuario u = new Usuario();
    u.setUsername(username);
    u.setPassword(passwordEncoder.encode(PASSWORD));
    u.setPersona(p);
    u.setRoles(new HashSet<>(Set.of(roles)));
    return userRepository.saveAndFlush(u);
  }

  protected void flushYLimpiar() {
    em.flush();
    em.clear();
  }

  // ---------------- HTTP ----------------

  protected String json(Object o) throws Exception {
    return objectMapper.writeValueAsString(o);
  }

  protected JsonNode cuerpo(MvcResult result) throws Exception {
    return objectMapper.readTree(result.getResponse().getContentAsString());
  }

  /** Inicia sesión por la API real y devuelve el JWT. */
  protected String loginYObtenerToken(String username, String password) throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername(username);
    request.setPassword(password);
    MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(json(request)))
        .andReturn();
    if (result.getResponse().getStatus() != 200) {
      throw new IllegalStateException("Login falló para " + username + ": "
          + result.getResponse().getStatus() + " " + result.getResponse().getContentAsString());
    }
    return cuerpo(result).path("data").path("token").asText();
  }

  protected String bearer(String username) throws Exception {
    return "Bearer " + loginYObtenerToken(username, PASSWORD);
  }

  protected MockHttpServletRequestBuilder conToken(MockHttpServletRequestBuilder builder, String bearer) {
    return builder.header("Authorization", bearer);
  }
}
