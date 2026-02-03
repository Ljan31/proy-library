package com.proyecto.fhce.library.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.fhce.library.entities.Permiso;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.PermisoRepository;
import com.proyecto.fhce.library.repositories.PersonaRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.security.RoleName;

@Service
public class DataSeedService {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PersonaRepository personaRepository;
  private final PermisoRepository permisoRepository;
  private final PasswordEncoder passwordEncoder;

  public DataSeedService(RoleRepository roleRepository,
      UserRepository userRepository, PersonaRepository personaRepository, PermisoRepository permisoRepository,
      PasswordEncoder passwordEncoder) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.personaRepository = personaRepository;
    this.permisoRepository = permisoRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public String seed() {
    // 1️⃣ Crear roles
    for (RoleName roleName : RoleName.values()) {
      roleRepository.findByName(roleName.name())
          .orElseGet(() -> {
            Role role = new Role();
            role.setName(roleName.name());
            return roleRepository.save(role);
          });
    }
    // ==================== 2️⃣ PERMISOS ====================
    if (permisoRepository.count() == 0) {
      String[] modulos = { "USUARIOS", "LIBROS", "PRESTAMOS", "RESERVAS", "SANCIONES", "CERTIFICADOS", "REPORTES" };
      String[] acciones = { "VER", "CREAR", "EDITAR", "ELIMINAR" };

      for (String modulo : modulos) {
        for (String accion : acciones) {
          Permiso permiso = new Permiso();
          permiso.setNombre_permiso(modulo + "_" + accion);
          permiso.setDescripcion(accion + " " + modulo.toLowerCase());
          permiso.setModulo(modulo);
          permisoRepository.save(permiso);
        }
      }

      System.out.println("Permisos por defecto creados");
    }
    // ==================== 3️⃣ CREAR USUARIOS ====================
    createUserIfNotExists("admin", "Admin", "System", "n/n", 1111,
        "admin@localhost", "11111", "Admin123!", RoleName.ROLE_ADMIN);

    createUserIfNotExists("bibliotecario", "Juan", "Perez", "Gomez", 1234567,
        "bibliotecario@localhost", "22222", "Biblio123!", RoleName.ROLE_BIBLIOTECARIO);

    createUserIfNotExists("estudiante1", "Maria", "Lopez", "Diaz", 7654321,
        "estudiante@localhost", "33333", "Estudiante123!", RoleName.ROLE_ESTUDIANTE);

    // ==================== 4️⃣ ASIGNAR PERMISOS A ROLES ====================
    assignPermisos();

    return "Data seed completed successfully ✅";
  }

  // ==================== MÉTODO AUXILIAR: CREAR USUARIOS ====================
  private void createUserIfNotExists(String username,
      String nombre, String apellidoPat, String apellidoMat, int ci,
      String email, String celular, String password,
      RoleName roleName) {
    if (!userRepository.existsByUsername(username)) {
      Role role = roleRepository.findByName(roleName.name())
          .orElseThrow(() -> new RuntimeException(roleName.name() + " no existe"));

      Persona persona = new Persona();
      persona.setNombre(nombre);
      persona.setApellido_pat(apellidoPat);
      persona.setApellido_mat(apellidoMat);
      persona.setCi(ci);
      persona.setEmail(email);
      persona.setCelular(celular);
      persona = personaRepository.save(persona);

      Usuario usuario = new Usuario();
      usuario.setUsername(username);
      usuario.setPassword(passwordEncoder.encode(password));
      usuario.setEnabled(true);
      usuario.setRoles(Set.of(role));
      usuario.setPersona(persona);

      userRepository.save(usuario);

      System.out.println("Usuario creado: " + username + " con rol " + roleName.name());
    }
  }

  // ==================== MÉTODO AUXILIAR: ASIGNAR PERMISOS ====================
  private void assignPermisos() {
    // Obtener todos los permisos
    List<Permiso> todosPermisos = permisoRepository.findAll();

    // ROLE_ADMIN = todos los permisos
    Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN.name()).orElseThrow();
    adminRole.setPermisos(new HashSet<>(todosPermisos));
    roleRepository.save(adminRole);

    // ROLE_BIBLIOTECARIO = solo prestamos, devoluciones, historial
    Role biblioRole = roleRepository.findByName(RoleName.ROLE_BIBLIOTECARIO.name()).orElseThrow();
    Set<Permiso> biblioPermisos = todosPermisos.stream()
        .filter(p -> p.getModulo().equals("PRESTAMOS") || p.getModulo().equals("RESERVAS"))
        .collect(Collectors.toSet());
    biblioRole.setPermisos(biblioPermisos);
    roleRepository.save(biblioRole);

    // ROLE_ESTUDIANTE = ver info propia, consultar recursos
    Role estudianteRole = roleRepository.findByName(RoleName.ROLE_ESTUDIANTE.name()).orElseThrow();
    Set<Permiso> estudiantePermisos = todosPermisos.stream()
        .filter(p -> p.getModulo().equals("LIBROS") && p.getNombre_permiso().startsWith("VER"))
        .collect(Collectors.toSet());
    estudianteRole.setPermisos(estudiantePermisos);
    roleRepository.save(estudianteRole);

    System.out.println("Permisos asignados a los roles ✅");
  }
}