package com.proyecto.fhce.library.iteracion1.unit;

import static com.proyecto.fhce.library.support.TestData.role;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.proyecto.fhce.library.dto.request.users.RoleRequest;
import com.proyecto.fhce.library.dto.response.users.RoleResponse;
import com.proyecto.fhce.library.entities.Permiso;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.PermisoRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.users.RoleServiceImpl;

@ExtendWith(MockitoExtension.class)
@DisplayName("Iteración 1 · RoleServiceImpl (unitarias)")
class RoleServiceImplTest {

  @Mock
  RoleRepository roleRepository;
  @Mock
  PermisoRepository permisoRepository;
  @Mock
  UserRepository usuarioRepository;

  // RoleServiceImpl usa inyección por campo (@Autowired); Mockito la soporta.
  @InjectMocks
  RoleServiceImpl service;

  private RoleRequest request(String nombre, Set<Long> permisoIds) {
    RoleRequest r = new RoleRequest();
    r.setName(nombre);
    r.setPermisoIds(permisoIds);
    return r;
  }

  private Permiso permiso(Long id, String nombre) {
    Permiso p = new Permiso();
    p.setId_permiso(id);
    p.setNombre_permiso(nombre);
    p.setModulo("TEST");
    return p;
  }

  @Test
  @DisplayName("IT1-U-ROL-01: crear rol con permisos los asocia al rol")
  void create_conPermisos_ok() {
    when(roleRepository.existsByName("ROLE_TEST")).thenReturn(false);
    when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso(1L, "LIBROS_VER")));
    when(roleRepository.save(any(Role.class))).thenAnswer(inv -> {
      Role r = inv.getArgument(0);
      r.setId_role(9L);
      return r;
    });

    RoleResponse response = service.create(request("ROLE_TEST", Set.of(1L)));

    assertThat(response.getId_role()).isEqualTo(9L);
    assertThat(response.getName()).isEqualTo("ROLE_TEST");
    assertThat(response.getPermisos()).hasSize(1);
  }

  @Test
  @DisplayName("IT1-U-ROL-02: crear rol con nombre repetido lanza DuplicateResourceException")
  void create_nombreDuplicado() {
    when(roleRepository.existsByName("ROLE_ADMIN")).thenReturn(true);

    assertThatThrownBy(() -> service.create(request("ROLE_ADMIN", null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Ya existe un rol con nombre: ROLE_ADMIN");
    verify(roleRepository, never()).save(any());
  }

  @Test
  @DisplayName("IT1-U-ROL-03: crear rol con permiso inexistente lanza ResourceNotFoundException")
  void create_permisoInexistente() {
    when(roleRepository.existsByName("ROLE_TEST")).thenReturn(false);
    when(permisoRepository.findById(50L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.create(request("ROLE_TEST", Set.of(50L))))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Permiso no encontrado con id: 50");
  }

  @Test
  @DisplayName("IT1-U-ROL-04: no se elimina un rol que tiene usuarios asignados")
  void delete_conUsuarios() {
    Role r = role(2L, "ROLE_BIBLIOTECARIO");
    when(roleRepository.findById(2L)).thenReturn(Optional.of(r));
    when(usuarioRepository.countByRoleName("ROLE_BIBLIOTECARIO")).thenReturn(3L);

    assertThatThrownBy(() -> service.delete(2L))
        .isInstanceOf(BusinessException.class)
        .hasMessageContaining("3 usuarios asignados");
    verify(roleRepository, never()).delete(any());
  }

  @Test
  @DisplayName("IT1-U-ROL-05: se elimina un rol sin usuarios")
  void delete_sinUsuarios_ok() {
    Role r = role(2L, "ROLE_TEST");
    when(roleRepository.findById(2L)).thenReturn(Optional.of(r));
    when(usuarioRepository.countByRoleName("ROLE_TEST")).thenReturn(0L);

    service.delete(2L);

    verify(roleRepository).delete(r);
  }

  @Test
  @DisplayName("IT1-U-ROL-06: actualizar rol a un nombre ya usado por otro rol lanza DuplicateResourceException")
  void update_nombreDuplicado() {
    when(roleRepository.findById(2L)).thenReturn(Optional.of(role(2L, "ROLE_A")));
    when(roleRepository.existsByName("ROLE_B")).thenReturn(true);

    assertThatThrownBy(() -> service.update(request("ROLE_B", null), 2L))
        .isInstanceOf(DuplicateResourceException.class);
  }

  @Test
  @DisplayName("IT1-U-ROL-07: actualizar rol conservando su nombre no valida duplicado")
  void update_mismoNombre_ok() {
    Role existente = role(2L, "ROLE_A");
    when(roleRepository.findById(2L)).thenReturn(Optional.of(existente));
    when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

    RoleResponse response = service.update(request("ROLE_A", null), 2L);

    assertThat(response.getName()).isEqualTo("ROLE_A");
    verify(roleRepository, never()).existsByName(any());
  }

  @Test
  @DisplayName("IT1-U-ROL-08: asignar un permiso ya asignado lanza BusinessException")
  void asignarPermiso_yaAsignado() {
    Permiso p = permiso(1L, "LIBROS_VER");
    Role r = role(2L, "ROLE_A");
    r.setPermisos(new HashSet<>(Set.of(p)));
    when(roleRepository.findById(2L)).thenReturn(Optional.of(r));
    when(permisoRepository.findById(1L)).thenReturn(Optional.of(p));

    assertThatThrownBy(() -> service.asignarPermiso(2L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessage("El rol ya tiene asignado este permiso");
  }

  @Test
  @DisplayName("IT1-U-ROL-09: asignar un permiso nuevo lo agrega al rol")
  void asignarPermiso_ok() {
    Permiso p = permiso(1L, "LIBROS_VER");
    Role r = role(2L, "ROLE_A");
    r.setPermisos(new HashSet<>());
    when(roleRepository.findById(2L)).thenReturn(Optional.of(r));
    when(permisoRepository.findById(1L)).thenReturn(Optional.of(p));
    when(roleRepository.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));

    RoleResponse response = service.asignarPermiso(2L, 1L);

    assertThat(response.getPermisos()).hasSize(1);
  }

  @Test
  @DisplayName("IT1-U-ROL-10: remover un permiso que el rol no tiene lanza BusinessException")
  void removerPermiso_noAsignado() {
    Role r = role(2L, "ROLE_A");
    r.setPermisos(new HashSet<>());
    when(roleRepository.findById(2L)).thenReturn(Optional.of(r));
    when(permisoRepository.findById(1L)).thenReturn(Optional.of(permiso(1L, "LIBROS_VER")));

    assertThatThrownBy(() -> service.removerPermiso(2L, 1L))
        .isInstanceOf(BusinessException.class)
        .hasMessage("El rol no tiene asignado este permiso");
  }

  @Test
  @DisplayName("IT1-U-ROL-11: buscar un rol inexistente por id lanza ResourceNotFoundException")
  void findById_noExiste() {
    when(roleRepository.findByIdWithPermisos(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(99L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessage("Rol no encontrado con id: 99");
  }
}
