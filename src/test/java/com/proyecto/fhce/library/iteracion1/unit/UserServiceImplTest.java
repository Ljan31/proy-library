package com.proyecto.fhce.library.iteracion1.unit;

import static com.proyecto.fhce.library.support.TestData.persona;
import static com.proyecto.fhce.library.support.TestData.role;
import static com.proyecto.fhce.library.support.TestData.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.proyecto.fhce.library.dto.request.users.ChangePasswordRequest;
import com.proyecto.fhce.library.dto.request.users.PersonaRequest;
import com.proyecto.fhce.library.dto.request.users.RegisterRequest;
import com.proyecto.fhce.library.dto.request.users.RegisterRequestEst;
import com.proyecto.fhce.library.dto.request.users.UsuarioCarreraRequestEst;
import com.proyecto.fhce.library.dto.request.users.UsuarioUpdateRequest;
import com.proyecto.fhce.library.dto.response.users.AdminResetPasswordResponse;
import com.proyecto.fhce.library.dto.response.users.PersonaResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioResponse;
import com.proyecto.fhce.library.entities.Carrera;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.exception.BadRequestException;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaEncargadoRepository;
import com.proyecto.fhce.library.repositories.CarreraRepository;
import com.proyecto.fhce.library.repositories.PersonaRepository;
import com.proyecto.fhce.library.repositories.PrestamoRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.users.PersonaService;
import com.proyecto.fhce.library.services.users.UserServiceImpl;

/**
 * Iteración 1 - Pruebas unitarias de UserServiceImpl.
 * Todas las dependencias (repositorios, encoder, auditoría) están simuladas
 * con Mockito: se prueba SOLO la lógica de negocio del servicio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 1 · UserServiceImpl (unitarias)")
class UserServiceImplTest {

  @Mock
  UserRepository usuarioRepository;
  @Mock
  PersonaService personaService;
  @Mock
  PersonaRepository personaRepository;
  @Mock
  RoleRepository roleRepository;
  @Mock
  BibliotecaEncargadoRepository bibliotecaEncargadoRepository;
  @Mock
  CarreraRepository carreraRepository;
  @Mock
  PasswordEncoder passwordEncoder;
  @Mock
  PrestamoRepository prestamoRepository;
  @Mock
  AuditoriaService auditoriaService;

  @InjectMocks
  UserServiceImpl service;

  // ---------- helpers ----------

  private RegisterRequest registerRequest(Set<Long> roleIds) {
    PersonaRequest p = new PersonaRequest();
    p.setNombre("Ana");
    p.setApellido_pat("Lopez");
    p.setCi("7654321");
    p.setEmail("ana@correo.com");

    RegisterRequest r = new RegisterRequest();
    r.setUsername("analopez");
    r.setPassword("secreto123");
    r.setPersona(p);
    r.setRoleIds(roleIds);
    return r;
  }

  /** Simula que el repositorio asigna el id al guardar (como haría JPA). */
  private void stubSaveAsignaId(Long id) {
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
      Usuario u = inv.getArgument(0);
      u.setId_usuario(id);
      return u;
    });
  }

  private void stubPersonaCreada(Long idPersona) {
    PersonaResponse pr = new PersonaResponse();
    pr.setId_persona(idPersona);
    when(personaService.create(any(PersonaRequest.class))).thenReturn(pr);
    when(personaRepository.findById(idPersona)).thenReturn(Optional.of(persona(idPersona, "7654321")));
  }

  // ---------- create ----------

  @Test
  @DisplayName("IT1-U-USR-01: crear usuario sin roles asigna ROLE_ESTUDIANTE y encripta la contraseña")
  void create_sinRoles_asignaRolEstudianteYEncriptaPassword() {
    Role estudiante = role(4L, "ROLE_ESTUDIANTE");
    when(usuarioRepository.existsByUsername("analopez")).thenReturn(false);
    stubPersonaCreada(1L);
    when(roleRepository.findByName("ROLE_ESTUDIANTE")).thenReturn(Optional.of(estudiante));
    when(passwordEncoder.encode("secreto123")).thenReturn("HASH");
    stubSaveAsignaId(10L);

    UsuarioResponse response = service.create(registerRequest(null));

    ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
    verify(usuarioRepository).save(captor.capture());
    Usuario guardado = captor.getValue();
    assertThat(guardado.getPassword()).isEqualTo("HASH").isNotEqualTo("secreto123");
    assertThat(guardado.isEnabled()).isTrue();
    assertThat(guardado.getRoles()).extracting(Role::getName).containsExactly("ROLE_ESTUDIANTE");

    assertThat(response.getId_usuario()).isEqualTo(10L);
    assertThat(response.getUsername()).isEqualTo("analopez");
    verify(auditoriaService).registrar(eq("CREAR_USUARIO"), eq("usuarios"), eq(10L), isNull(), any());
  }

  @Test
  @DisplayName("IT1-U-USR-02: crear usuario con roleIds asigna exactamente esos roles")
  void create_conRoleIds_asignaLosRolesIndicados() {
    when(usuarioRepository.existsByUsername("analopez")).thenReturn(false);
    stubPersonaCreada(1L);
    when(roleRepository.findById(1L)).thenReturn(Optional.of(role(1L, "ROLE_ADMIN")));
    when(roleRepository.findById(2L)).thenReturn(Optional.of(role(2L, "ROLE_BIBLIOTECARIO")));
    when(passwordEncoder.encode(anyString())).thenReturn("HASH");
    stubSaveAsignaId(11L);

    UsuarioResponse response = service.create(registerRequest(Set.of(1L, 2L)));

    assertThat(response.getRoles()).extracting(r -> r.getName())
        .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_BIBLIOTECARIO");
    verify(roleRepository, never()).findByName(anyString());
  }

  @Test
  @DisplayName("IT1-U-USR-03: crear usuario con username repetido lanza DuplicateResourceException")
  void create_usernameDuplicado_lanzaExcepcionYNoCreaPersona() {
    when(usuarioRepository.existsByUsername("analopez")).thenReturn(true);

    assertThatThrownBy(() -> service.create(registerRequest(null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Username ya existe");

    verifyNoInteractions(personaService);
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-USR-04: crear usuario con roleId inexistente lanza ResourceNotFoundException")
  void create_roleInexistente_lanzaResourceNotFound() {
    when(usuarioRepository.existsByUsername("analopez")).thenReturn(false);
    stubPersonaCreada(1L);
    when(passwordEncoder.encode(anyString())).thenReturn("HASH");
    when(roleRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(registerRequest(Set.of(99L))))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Rol no encontrado: 99");
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-USR-05: crear usuario sin ROLE_ESTUDIANTE en BD lanza ResourceNotFoundException")
  void create_sinRolPorDefectoEnBD_lanzaResourceNotFound() {
    when(usuarioRepository.existsByUsername("analopez")).thenReturn(false);
    stubPersonaCreada(1L);
    when(passwordEncoder.encode(anyString())).thenReturn("HASH");
    when(roleRepository.findByName("ROLE_ESTUDIANTE")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(registerRequest(null)))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Rol por defecto no encontrado");
  }

  // ---------- createEst (registro público de estudiante) ----------

  @Test
  @DisplayName("IT1-U-USR-06: registro de estudiante rechaza la misma carrera repetida en el request")
  void createEst_carreraRepetida_lanzaDuplicateResource() {
    when(usuarioRepository.existsByUsername("analopez")).thenReturn(false);
    stubPersonaCreada(1L);
    when(passwordEncoder.encode(anyString())).thenReturn("HASH");
    when(roleRepository.findByName("ROLE_ESTUDIANTE")).thenReturn(Optional.of(role(4L, "ROLE_ESTUDIANTE")));
    stubSaveAsignaId(12L);
    Carrera carrera = new Carrera();
    carrera.setIdCarrera(5L);
    when(carreraRepository.findById(5L)).thenReturn(Optional.of(carrera));

    UsuarioCarreraRequestEst c1 = new UsuarioCarreraRequestEst();
    c1.setCarreraId(5L);
    c1.setMatricula("2020-001");
    UsuarioCarreraRequestEst c2 = new UsuarioCarreraRequestEst();
    c2.setCarreraId(5L);
    c2.setMatricula("2020-002");

    RegisterRequestEst request = new RegisterRequestEst();
    request.setUsername("analopez");
    request.setPassword("secreto123");
    request.setPersona(registerRequest(null).getPersona());
    request.setUserCarreras(Set.of(c1, c2));

    assertThatThrownBy(() -> service.createEst(request))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Carrera duplicada en el request: 5");
  }

  // ---------- update ----------

  @Test
  @DisplayName("IT1-U-USR-07: actualizar usuario con CI ya usado por otra persona lanza DuplicateResourceException")
  void update_ciDeOtraPersona_lanzaDuplicateResource() {
    Usuario existente = usuario(1L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    existente.getPersona().setCi("1111111");
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
    when(personaRepository.existsByCi("2222222")).thenReturn(true);

    UsuarioUpdateRequest request = new UsuarioUpdateRequest();
    request.setCi("2222222");

    assertThatThrownBy(() -> service.update(1L, request))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("Ya existe una persona con CI: 2222222");
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-USR-08: actualizar usuario modifica solo los campos enviados (no nulos)")
  void update_soloCamposNoNulos_conservaElResto() {
    Usuario existente = usuario(1L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

    UsuarioUpdateRequest request = new UsuarioUpdateRequest();
    request.setNombre("Carlos");
    request.setCelular("77777777");

    service.update(1L, request);

    Persona p = existente.getPersona();
    assertThat(p.getNombre()).isEqualTo("Carlos");
    assertThat(p.getCelular()).isEqualTo("77777777");
    assertThat(p.getApellido_pat()).isEqualTo("Perez"); // intacto
    assertThat(existente.getUsername()).isEqualTo("juan"); // intacto
    verify(auditoriaService).registrar(eq("ACTUALIZAR_USUARIO"), eq("usuarios"), eq(1L), any(), any());
  }

  @Test
  @DisplayName("IT1-U-USR-09: actualizar usuario inexistente lanza ResourceNotFoundException")
  void update_noExiste_lanzaResourceNotFound() {
    when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(99L, new UsuarioUpdateRequest()))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Usuario no encontrado con id: 99");
  }

  // ---------- changePassword ----------

  private ChangePasswordRequest cambio(String actual, String nueva, String confirma) {
    ChangePasswordRequest r = new ChangePasswordRequest();
    r.setCurrentPassword(actual);
    r.setNewPassword(nueva);
    r.setConfirmPassword(confirma);
    return r;
  }

  @Test
  @DisplayName("IT1-U-USR-10: cambiar contraseña con la actual incorrecta lanza BadRequestException")
  void changePassword_actualIncorrecta_lanzaBadRequest() {
    Usuario u = usuario(1L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(u));
    when(passwordEncoder.matches("mala", u.getPassword())).thenReturn(false);

    assertThatThrownBy(() -> service.changePassword("juan", cambio("mala", "nueva123", "nueva123")))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Password actual incorrecto");
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-USR-11: cambiar contraseña con confirmación distinta lanza BadRequestException")
  void changePassword_confirmacionDistinta_lanzaBadRequest() {
    Usuario u = usuario(1L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(u));
    when(passwordEncoder.matches("actual", u.getPassword())).thenReturn(true);

    assertThatThrownBy(() -> service.changePassword("juan", cambio("actual", "nueva123", "otra1234")))
        .isInstanceOf(BadRequestException.class)
        .hasMessage("Los passwords no coinciden");
  }

  @Test
  @DisplayName("IT1-U-USR-12: cambiar contraseña correctamente guarda el nuevo hash y audita")
  void changePassword_ok_guardaHashYAudita() {
    Usuario u = usuario(1L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findByUsername("juan")).thenReturn(Optional.of(u));
    when(passwordEncoder.matches("actual", u.getPassword())).thenReturn(true);
    when(passwordEncoder.encode("nueva123")).thenReturn("NUEVO_HASH");

    service.changePassword("juan", cambio("actual", "nueva123", "nueva123"));

    assertThat(u.getPassword()).isEqualTo("NUEVO_HASH");
    verify(usuarioRepository).save(u);
    verify(auditoriaService).registrar(eq("CAMBIAR_PASSWORD"), eq("usuarios"), eq(1L), isNull(), any());
  }

  // ---------- adminResetPassword ----------

  @Test
  @DisplayName("IT1-U-USR-13: no se puede resetear la contraseña de un administrador")
  void adminResetPassword_aAdmin_lanzaBadRequest() {
    Usuario admin = usuario(1L, "admin", role(1L, "ROLE_ADMIN"));
    when(usuarioRepository.findById(1L)).thenReturn(Optional.of(admin));

    assertThatThrownBy(() -> service.adminResetPassword(1L))
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("administrador");
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-USR-14: resetear contraseña genera una temporal de 8 caracteres y guarda su hash")
  void adminResetPassword_ok_generaPasswordTemporal() {
    Usuario est = usuario(2L, "estudiante", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(est));
    when(passwordEncoder.encode(anyString())).thenReturn("HASH_TEMPORAL");

    AdminResetPasswordResponse response = service.adminResetPassword(2L);

    assertThat(response.getTemporaryPassword()).hasSize(8);
    verify(passwordEncoder).encode(response.getTemporaryPassword());
    assertThat(est.getPassword()).isEqualTo("HASH_TEMPORAL");
    verify(auditoriaService).registrar(eq("RESTABLECER_PASSWORD_ADMIN"), eq("usuarios"), eq(2L), isNull(), any());
  }

  // ---------- toggleEnabled ----------

  @Test
  @DisplayName("IT1-U-USR-15: toggleEnabled desactiva un usuario activo y audita DESACTIVAR_USUARIO")
  void toggleEnabled_activo_loDesactiva() {
    Usuario u = usuario(3L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findById(3L)).thenReturn(Optional.of(u));
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

    service.toggleEnabled(3L);

    assertThat(u.isEnabled()).isFalse();
    verify(auditoriaService).registrar("DESACTIVAR_USUARIO", "usuarios", 3L, "enabled=true", "enabled=false");
  }

  @Test
  @DisplayName("IT1-U-USR-16: toggleEnabled reactiva un usuario inactivo y audita ACTIVAR_USUARIO")
  void toggleEnabled_inactivo_loActiva() {
    Usuario u = usuario(3L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    u.setEnabled(false);
    when(usuarioRepository.findById(3L)).thenReturn(Optional.of(u));
    when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

    service.toggleEnabled(3L);

    assertThat(u.isEnabled()).isTrue();
    verify(auditoriaService).registrar("ACTIVAR_USUARIO", "usuarios", 3L, "enabled=false", "enabled=true");
  }

  // ---------- delete ----------

  @Test
  @DisplayName("IT1-U-USR-17: no se elimina un usuario con préstamos registrados")
  void delete_conPrestamos_lanzaBusinessException() {
    Usuario u = usuario(4L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findById(4L)).thenReturn(Optional.of(u));
    when(prestamoRepository.existsByUsuario_IdUsuario(4L)).thenReturn(true);

    assertThatThrownBy(() -> service.delete(4L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("préstamos registrados");
    verify(usuarioRepository, never()).delete(any());
  }

  @Test
  @DisplayName("IT1-U-USR-18: no se elimina un usuario con historial como encargado de biblioteca")
  void delete_conHistorialDeEncargado_lanzaBusinessException() {
    Usuario u = usuario(4L, "juan", role(3L, "ROLE_BIBLIOTECARIO"));
    when(usuarioRepository.findById(4L)).thenReturn(Optional.of(u));
    when(prestamoRepository.existsByUsuario_IdUsuario(4L)).thenReturn(false);
    when(bibliotecaEncargadoRepository.existsByUsuario_IdUsuario(4L)).thenReturn(true);

    assertThatThrownBy(() -> service.delete(4L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("historial como encargado");
    verify(usuarioRepository, never()).delete(any());
  }

  @Test
  @DisplayName("IT1-U-USR-19: eliminar un usuario sin dependencias lo borra y audita ELIMINAR_USUARIO")
  void delete_sinDependencias_loElimina() {
    Usuario u = usuario(4L, "juan", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findById(4L)).thenReturn(Optional.of(u));

    service.delete(4L);

    verify(usuarioRepository).delete(u);
    verify(auditoriaService).registrar(eq("ELIMINAR_USUARIO"), eq("usuarios"), eq(4L), any(), isNull());
  }

  // ---------- consultas ----------

  @Test
  @DisplayName("IT1-U-USR-20: findById de usuario inexistente lanza ResourceNotFoundException")
  void findById_noExiste_lanzaResourceNotFound() {
    when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Usuario no encontrado con id: 99");
  }

  @Test
  @DisplayName("IT1-U-USR-21: findByRoleName agrega el prefijo ROLE_ si no viene")
  void findByRoleName_sinPrefijo_loAgrega() {
    Usuario admin = usuario(1L, "admin", role(1L, "ROLE_ADMIN"));
    when(usuarioRepository.findByRoleName("ROLE_ADMIN")).thenReturn(List.of(admin));

    List<UsuarioResponse> resultado = service.findByRoleName("ADMIN");

    assertThat(resultado).hasSize(1);
    assertThat(resultado.get(0).getUsername()).isEqualTo("admin");
    verify(usuarioRepository).findByRoleName("ROLE_ADMIN");
  }

  @Test
  @DisplayName("IT1-U-USR-22: findByRoleName respeta el prefijo ROLE_ si ya viene")
  void findByRoleName_conPrefijo_noLoDuplica() {
    when(usuarioRepository.findByRoleName("ROLE_ESTUDIANTE")).thenReturn(List.of());

    assertThat(service.findByRoleName("ROLE_ESTUDIANTE")).isEmpty();
    verify(usuarioRepository).findByRoleName("ROLE_ESTUDIANTE");
  }
}
