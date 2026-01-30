package com.proyecto.fhce.library.services;

import java.util.Set;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.PersonaRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.security.RoleName;

@Service
public class DataSeedService {

  private final RoleRepository roleRepository;
  private final UserRepository userRepository;
  private final PersonaRepository personaRepository;
  private final PasswordEncoder passwordEncoder;

  public DataSeedService(RoleRepository roleRepository,
      UserRepository userRepository, PersonaRepository personaRepository,
      PasswordEncoder passwordEncoder) {
    this.roleRepository = roleRepository;
    this.userRepository = userRepository;
    this.personaRepository = personaRepository;
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

    // 2️⃣ Crear admin
    if (!userRepository.existsByUsername("admin")) {
      Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN.name())
          .orElseThrow(() -> new RuntimeException("ROLE_ADMIN no existe"));

      Persona adminPersona = new Persona();
      adminPersona.setNombre("Admin");
      adminPersona.setApellido_pat("System");
      adminPersona.setApellido_mat("n/n");
      adminPersona.setCi(0000000);
      adminPersona.setEmail("admin@localhost");
      adminPersona.setCelular("11111");
      adminPersona = personaRepository.save(adminPersona);

      Usuario admin = new Usuario();
      admin.setUsername("admin");
      admin.setPassword(passwordEncoder.encode("Admin123!"));
      admin.setEnabled(true);
      admin.setRoles(Set.of(adminRole));
      admin.setPersona(adminPersona);

      userRepository.save(admin);
    }

    return "Data seed completed successfully ✅";
  }
}