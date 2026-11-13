package com.proyecto.fhce.library.iteracion1.unit;

import static com.proyecto.fhce.library.support.TestData.persona;
import static com.proyecto.fhce.library.support.TestData.role;
import static com.proyecto.fhce.library.support.TestData.usuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.proyecto.fhce.library.dto.request.users.PersonaRequest;
import com.proyecto.fhce.library.dto.response.users.PersonaResponse;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.PersonaRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.users.PersonaServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 1 · PersonaServiceImpl (unitarias)")
class PersonaServiceImplTest {

  @Mock
  PersonaRepository personaRepository;
  @Mock
  UserRepository userRepository;
  @Mock
  AuditoriaService auditoriaService;

  @InjectMocks
  PersonaServiceImpl service;

  private PersonaRequest request(String ci, String email) {
    PersonaRequest r = new PersonaRequest();
    r.setNombre("Maria");
    r.setApellido_pat("Quispe");
    r.setApellido_mat("Mamani");
    r.setCi(ci);
    r.setCelular("71234567");
    r.setEmail(email);
    return r;
  }

  @Test
  @DisplayName("IT1-U-PER-01: crear persona válida la guarda, arma el nombre completo y audita")
  void create_ok() {
    when(personaRepository.existsByCi("1234567")).thenReturn(false);
    when(personaRepository.existsByEmail("maria@correo.com")).thenReturn(false);
    when(personaRepository.save(any(Persona.class))).thenAnswer(inv -> {
      Persona p = inv.getArgument(0);
      p.setId_persona(7L);
      return p;
    });

    PersonaResponse response = service.create(request("1234567", "maria@correo.com"));

    assertThat(response.getId_persona()).isEqualTo(7L);
    assertThat(response.getNombreCompleto()).isEqualTo("Maria Quispe Mamani");
    assertThat(response.getCi()).isEqualTo("1234567");
    verify(auditoriaService).registrar(eq("CREAR"), eq("Personas"), eq(7L), isNull(), any());
  }

  @Test
  @DisplayName("IT1-U-PER-02: crear persona con CI repetido lanza DuplicateResourceException")
  void create_ciDuplicado() {
    when(personaRepository.existsByCi("1234567")).thenReturn(true);

    assertThatThrownBy(() -> service.create(request("1234567", "maria@correo.com")))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Ya existe una persona con CI: 1234567");
    verify(personaRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-PER-03: crear persona con email repetido lanza DuplicateResourceException")
  void create_emailDuplicado() {
    when(personaRepository.existsByCi("1234567")).thenReturn(false);
    when(personaRepository.existsByEmail("maria@correo.com")).thenReturn(true);

    assertThatThrownBy(() -> service.create(request("1234567", "maria@correo.com")))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Ya existe una persona con email: maria@correo.com");
  }

  @Test
  @DisplayName("IT1-U-PER-04: crear persona sin email no consulta la unicidad del email")
  void create_sinEmail_noValidaEmail() {
    when(personaRepository.existsByCi("1234567")).thenReturn(false);
    when(personaRepository.save(any(Persona.class))).thenAnswer(inv -> inv.getArgument(0));

    service.create(request("1234567", null));

    verify(personaRepository, never()).existsByEmail(any());
  }

  @Test
  @DisplayName("IT1-U-PER-05: actualizar persona inexistente lanza ResourceNotFoundException")
  void update_noExiste() {
    when(personaRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.update(99L, request("1234567", null)))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Persona no encontrada con id: 99");
  }

  @Test
  @DisplayName("IT1-U-PER-06: actualizar persona con email de otra persona lanza DuplicateResourceException")
  void update_emailDeOtraPersona() {
    Persona existente = persona(1L, "1234567");
    when(personaRepository.findById(1L)).thenReturn(Optional.of(existente));
    when(personaRepository.existsByEmail("otro@correo.com")).thenReturn(true);
    // CI distinto -> también se consulta existsByCi (devuelve false)
    when(personaRepository.existsByCi("7654321")).thenReturn(false);

    assertThatThrownBy(() -> service.update(1L, request("7654321", "otro@correo.com")))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessageContaining("email");
  }

  /**
   * DEFECTO DETECTADO (DEF-01): PersonaServiceImpl.update compara el CI con
   * {@code persona.getCi() != request.getCi()} (comparación de REFERENCIAS, no de
   * contenido). En producción, el CI de la BD y el del JSON son objetos String
   * distintos, así que al guardar la persona SIN cambiar su CI se lanza un falso
   * "CI duplicado". Corregir con {@code !Objects.equals(...)} y quitar @Disabled.
   * (Se usa new String(...) para forzar dos instancias distintas, como en
   * producción.)
   */
  @Test
  // @Disabled("DEF-01: update() compara Strings con '!=' en lugar de equals().
  // Habilitar tras corregir.")
  @DisplayName("IT1-U-PER-07: actualizar persona conservando su mismo CI NO debe lanzar duplicado")
  void update_mismoCi_noDebeFallar() {
    Persona existente = persona(1L, new String("1234567"));
    when(personaRepository.findById(1L)).thenReturn(Optional.of(existente));
    when(personaRepository.save(any(Persona.class))).thenAnswer(inv -> inv.getArgument(0));

    PersonaResponse response = service.update(1L, request(new String("1234567"), null));

    assertThat(response.getCi()).isEqualTo("1234567");
    verify(personaRepository, never()).existsByCi(any());
  }

  @Test
  @DisplayName("IT1-U-PER-08: eliminar persona existente la borra y audita ELIMINAR")
  void delete_ok() {
    Persona existente = persona(1L, "1234567");
    when(personaRepository.findById(1L)).thenReturn(Optional.of(existente));

    service.delete(1L);

    verify(personaRepository).delete(existente);
    verify(auditoriaService).registrar(eq("ELIMINAR"), eq("personas"), eq(1L), any(), isNull());
  }

  @Test
  @DisplayName("IT1-U-PER-09: eliminar persona inexistente lanza ResourceNotFoundException")
  void delete_noExiste() {
    when(personaRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.delete(99L)).isInstanceOf(ResourceNotFoundException.class);
    verify(personaRepository, never()).delete(any());
  }

  @Test
  @DisplayName("IT1-U-PER-10: nombre completo omite el apellido materno cuando es nulo")
  void findById_sinApellidoMaterno() {
    Persona p = persona(1L, "1234567");
    p.setApellido_mat(null);
    when(personaRepository.findById(1L)).thenReturn(Optional.of(p));

    assertThat(service.findById(1L).getNombreCompleto()).isEqualTo("Juan Perez");
  }

  // ---------- findByIdSecure: control de acceso a datos personales ----------

  @Test
  @DisplayName("IT1-U-PER-11: un estudiante NO puede ver los datos de otra persona (AccessDenied)")
  void findByIdSecure_estudianteOtraPersona_denegado() {
    Usuario estudiante = usuario(5L, "estud", role(4L, "ROLE_ESTUDIANTE")); // su persona tiene id 5
    when(userRepository.findByUsername("estud")).thenReturn(Optional.of(estudiante));

    assertThatThrownBy(() -> service.findByIdSecure(77L, "estud"))
        .isInstanceOf(AccessDeniedException.class);
    verify(personaRepository, never()).findById(any());
  }

  @Test
  @DisplayName("IT1-U-PER-12: un estudiante SÍ puede ver su propia persona")
  void findByIdSecure_estudiantePropiaPersona_permitido() {
    Usuario estudiante = usuario(5L, "estud", role(4L, "ROLE_ESTUDIANTE"));
    when(userRepository.findByUsername("estud")).thenReturn(Optional.of(estudiante));
    when(personaRepository.findById(5L)).thenReturn(Optional.of(estudiante.getPersona()));

    assertThat(service.findByIdSecure(5L, "estud").getId_persona()).isEqualTo(5L);
  }

  @Test
  @DisplayName("IT1-U-PER-13: un ADMIN puede ver cualquier persona")
  void findByIdSecure_admin_veCualquiera() {
    Usuario admin = usuario(1L, "admin", role(1L, "ROLE_ADMIN"));
    when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));
    when(personaRepository.findById(77L)).thenReturn(Optional.of(persona(77L, "9999999")));

    assertThat(service.findByIdSecure(77L, "admin").getId_persona()).isEqualTo(77L);
  }
}
