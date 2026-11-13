package com.proyecto.fhce.library.iteracion1.unit;

import static com.proyecto.fhce.library.support.TestData.biblioteca;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import com.proyecto.fhce.library.dto.request.AsignarEncargadoRequest;
import com.proyecto.fhce.library.dto.response.BibliotecaEncargadoResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.BibliotecaEncargado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.RolEncargado;
import com.proyecto.fhce.library.enums.TipoBiblioteca;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaEncargadoRepository;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.AuditoriaService;
import com.proyecto.fhce.library.services.BibliotecaEncargadoService;

/** Iteración 1 · tabla biblioteca_encargados: asignación y remoción de encargados. */
@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 1 · BibliotecaEncargadoService (unitarias)")
class BibliotecaEncargadoServiceTest {

  @Mock
  BibliotecaEncargadoRepository encargadoRepository;
  @Mock
  BibliotecaRepository bibliotecaRepository;
  @Mock
  UserRepository usuarioRepository;
  @Mock
  RoleRepository roleRepository;
  @Mock
  AuditoriaService auditoriaService;

  @InjectMocks
  BibliotecaEncargadoService service;

  @AfterEach
  void limpiarContextoDeSeguridad() {
    SecurityContextHolder.clearContext();
  }

  private AsignarEncargadoRequest request(Long usuarioId, RolEncargado rol) {
    AsignarEncargadoRequest r = new AsignarEncargadoRequest();
    r.setUsuarioId(usuarioId);
    r.setRolEncargado(rol);
    return r;
  }

  private Biblioteca bibliotecaExistente() {
    Biblioteca b = biblioteca(1L, "Biblioteca Historia", TipoBiblioteca.CARRERA);
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.of(b));
    return b;
  }

  // ---------- asignarEncargado ----------

  @Test
  @DisplayName("IT1-U-ENC-01: asignar encargado a una biblioteca inexistente lanza ResourceNotFoundException")
  void asignar_bibliotecaInexistente() {
    when(bibliotecaRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.asignarEncargado(1L, request(2L, RolEncargado.PRINCIPAL)))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Biblioteca no encontrada");
  }

  @Test
  @DisplayName("IT1-U-ENC-02: solo usuarios con rol BIBLIOTECARIO o ADMIN pueden ser encargados")
  void asignar_usuarioSinRolValido() {
    bibliotecaExistente();
    Usuario estudiante = usuario(2L, "estud", role(4L, "ROLE_ESTUDIANTE"));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(estudiante));

    assertThatThrownBy(() -> service.asignarEncargado(1L, request(2L, RolEncargado.PRINCIPAL)))
        .isInstanceOf(BusinessException.class)
        .hasMessage("El usuario debe tener rol BIBLIOTECARIO o ADMIN");
    verify(encargadoRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-ENC-03: un usuario ya encargado activo no puede asignarse dos veces")
  void asignar_yaEsEncargadoActivo() {
    bibliotecaExistente();
    Usuario bibliotecario = usuario(2L, "bib", role(3L, "ROLE_BIBLIOTECARIO"));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(true);

    assertThatThrownBy(() -> service.asignarEncargado(1L, request(2L, RolEncargado.PRINCIPAL)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("El usuario ya es encargado activo de esta biblioteca");
  }

  @Test
  @DisplayName("IT1-U-ENC-04: no puede haber dos encargados PRINCIPAL activos en la misma biblioteca")
  void asignar_segundoPrincipal() {
    bibliotecaExistente();
    Usuario bibliotecario = usuario(2L, "bib", role(3L, "ROLE_BIBLIOTECARIO"));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(false);
    when(encargadoRepository.findEncargadoPrincipal(1L, RolEncargado.PRINCIPAL))
        .thenReturn(Optional.of(new BibliotecaEncargado()));

    assertThatThrownBy(() -> service.asignarEncargado(1L, request(2L, RolEncargado.PRINCIPAL)))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Ya existe un encargado PRINCIPAL");
    verify(encargadoRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-ENC-05: asignar un encargado válido lo deja activo, con fecha de asignación, y audita")
  void asignar_ok() {
    bibliotecaExistente();
    Usuario bibliotecario = usuario(2L, "bib", role(3L, "ROLE_BIBLIOTECARIO"));
    when(usuarioRepository.findById(2L)).thenReturn(Optional.of(bibliotecario));
    when(encargadoRepository.existsByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(false);
    when(encargadoRepository.findEncargadoPrincipal(1L, RolEncargado.PRINCIPAL)).thenReturn(Optional.empty());
    when(encargadoRepository.save(any(BibliotecaEncargado.class))).thenAnswer(inv -> {
      BibliotecaEncargado be = inv.getArgument(0);
      be.setId(50L);
      return be;
    });

    BibliotecaEncargadoResponse response = service.asignarEncargado(1L, request(2L, RolEncargado.PRINCIPAL));

    assertThat(response.getId()).isEqualTo(50L);
    assertThat(response.getRolEncargado()).isEqualTo(RolEncargado.PRINCIPAL);
    assertThat(response.getActivo()).isTrue();
    assertThat(response.getFechaAsignacion()).isNotNull();
    assertThat(response.getUsuario().getUsername()).isEqualTo("bib");
    verify(auditoriaService).registrar(eq("ASIGNAR_ENCARGADO"), eq("biblioteca_encargados"), eq(50L), isNull(), any());
  }

  // ---------- removerEncargado ----------

  @Test
  @DisplayName("IT1-U-ENC-06: remover a quien no es encargado activo lanza ResourceNotFoundException")
  void remover_noEsEncargadoActivo() {
    bibliotecaExistente();
    when(encargadoRepository.findByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.removerEncargado(1L, 2L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("El usuario no es encargado activo de esta biblioteca");
  }

  @Test
  @DisplayName("IT1-U-ENC-07: al remover al último encargo de un AUXILIAR se le quita ROLE_AUXILIAR")
  void remover_auxiliarSinOtrosEncargos_pierdeRolAuxiliar() {
    Biblioteca b = bibliotecaExistente();
    Usuario auxiliar = usuario(2L, "aux", role(5L, "ROLE_AUXILIAR"));
    BibliotecaEncargado encargo = new BibliotecaEncargado();
    encargo.setId(60L);
    encargo.setBiblioteca(b);
    encargo.setUsuario(auxiliar);
    encargo.setRolEncargado(RolEncargado.AUXILIAR);
    when(encargadoRepository.findByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(Optional.of(encargo));
    when(encargadoRepository.existsByUsuario_IdUsuarioAndActivoTrue(2L)).thenReturn(false);
    when(roleRepository.findByName("ROLE_AUXILIAR")).thenReturn(Optional.of(role(5L, "ROLE_AUXILIAR")));

    service.removerEncargado(1L, 2L);

    verify(encargadoRepository).desactivarEncargado(eq(1L), eq(2L), any(LocalDateTime.class));
    assertThat(auxiliar.getRoles()).extracting(r -> r.getName()).doesNotContain("ROLE_AUXILIAR");
    verify(usuarioRepository).save(auxiliar);
    verify(auditoriaService).registrar(eq("REMOVER_ENCARGADO"), eq("biblioteca_encargados"), eq(60L), any(), any());
  }

  @Test
  @DisplayName("IT1-U-ENC-08: al remover un AUXILIAR con otros encargos activos conserva ROLE_AUXILIAR")
  void remover_auxiliarConOtrosEncargos_conservaRol() {
    Biblioteca b = bibliotecaExistente();
    Usuario auxiliar = usuario(2L, "aux", role(5L, "ROLE_AUXILIAR"));
    BibliotecaEncargado encargo = new BibliotecaEncargado();
    encargo.setId(61L);
    encargo.setBiblioteca(b);
    encargo.setUsuario(auxiliar);
    encargo.setRolEncargado(RolEncargado.AUXILIAR);
    when(encargadoRepository.findByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(Optional.of(encargo));
    when(encargadoRepository.existsByUsuario_IdUsuarioAndActivoTrue(2L)).thenReturn(true);

    service.removerEncargado(1L, 2L);

    assertThat(auxiliar.getRoles()).extracting(r -> r.getName()).contains("ROLE_AUXILIAR");
    verify(usuarioRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-ENC-09: un BIBLIOTECARIO no puede remover encargados de una biblioteca a la que no pertenece")
  void remover_bibliotecarioDeOtraBiblioteca() {
    Biblioteca b = bibliotecaExistente();
    Usuario auxiliar = usuario(2L, "aux", role(5L, "ROLE_AUXILIAR"));
    BibliotecaEncargado encargo = new BibliotecaEncargado();
    encargo.setBiblioteca(b);
    encargo.setUsuario(auxiliar);
    encargo.setRolEncargado(RolEncargado.AUXILIAR);
    when(encargadoRepository.findByBiblioteca_IdBibliotecaAndUsuario_IdUsuarioAndActivoTrue(1L, 2L))
        .thenReturn(Optional.of(encargo));

    // Usuario autenticado: bibliotecario que NO figura en b.getEncargados()
    Usuario bibliotecarioAjeno = usuario(9L, "bibAjeno", role(3L, "ROLE_BIBLIOTECARIO"));
    when(usuarioRepository.findByUsername("bibAjeno")).thenReturn(Optional.of(bibliotecarioAjeno));
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(new User("bibAjeno", "x", List.of()), null, List.of()));

    assertThatThrownBy(() -> service.removerEncargado(1L, 2L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("Solo puedes remover auxiliares");
    verify(encargadoRepository, never()).desactivarEncargado(any(), any(), any());
  }

  // ---------- consultas ----------

  @Test
  @DisplayName("IT1-U-ENC-10: listar encargados de una biblioteca inexistente lanza ResourceNotFoundException")
  void findEncargados_bibliotecaInexistente() {
    when(bibliotecaRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> service.findEncargadosByBiblioteca(1L))
        .isInstanceOf(ResourceNotFoundException.class);
  }
}
