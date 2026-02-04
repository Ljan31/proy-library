package com.proyecto.fhce.library.services.users;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.ChangePasswordRequest;
import com.proyecto.fhce.library.dto.request.RegisterRequest;
import com.proyecto.fhce.library.dto.request.UsuarioUpdateRequest;
import com.proyecto.fhce.library.dto.response.PersonaResponse;
import com.proyecto.fhce.library.dto.response.RoleSimpleResponse;
import com.proyecto.fhce.library.dto.response.UsuarioResponse;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.exception.BadRequestException;
import com.proyecto.fhce.library.exception.DuplicateResourceException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.PersonaRepository;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository usuarioRepository;

  @Autowired
  private PersonaService personaService;

  @Autowired
  private PersonaRepository personaRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  public UsuarioResponse create(RegisterRequest request) {
    if (usuarioRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("Username ya existe: " + request.getUsername());
    }

    // Crear persona
    PersonaResponse personaResponse = personaService.create(request.getPersona());
    // Persona persona = new Persona();
    // persona.setId_persona(personaResponse.getId_persona());
    Persona persona = personaRepository.findById(personaResponse.getId_persona())
        .orElseThrow(() -> new RuntimeException("Persona no encontrada"));

    // Crear usuario
    Usuario usuario = new Usuario();
    usuario.setUsername(request.getUsername());
    usuario.setPassword(passwordEncoder.encode(request.getPassword()));
    usuario.setEnabled(true);
    usuario.setPersona(persona);

    // Asignar roles
    Set<Role> roles = new HashSet<>();
    if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
      roles = request.getRoleIds().stream()
          .map(roleId -> roleRepository.findById(roleId)
              .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + roleId)))
          .collect(Collectors.toSet());
    } else {
      // Asignar rol por defecto (ESTUDIANTE)
      Role defaultRole = roleRepository.findByName("ROLE_ESTUDIANTE")
          .orElseThrow(() -> new ResourceNotFoundException("Rol por defecto no encontrado"));
      roles.add(defaultRole);
    }
    usuario.setRoles(roles);

    Usuario saved = usuarioRepository.save(usuario);
    // auditoriaService.registrar("CREATE_USER", "users", saved.getId_usuario(),
    // null, saved.getUsername());

    return mapToResponse(saved);
  }

  public UsuarioResponse update(Long id, UsuarioUpdateRequest request) {
    Usuario usuario = usuarioRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

    Persona persona = usuario.getPersona();

    if (request.getNombre() != null)
      persona.setNombre(request.getNombre());
    if (request.getApellido_pat() != null)
      persona.setApellido_pat(request.getApellido_pat());
    if (request.getApellido_mat() != null)
      persona.setApellido_mat(request.getApellido_mat());
    if (request.getCelular() != null)
      persona.setCelular(request.getCelular());
    if (request.getEmail() != null)
      persona.setEmail(request.getEmail());
    if (request.getMatricula() != null)
      persona.setMatricula(request.getMatricula());
    if (request.getEnabled() != null)
      usuario.setEnabled(request.getEnabled());

    Usuario updated = usuarioRepository.save(usuario);
    // auditoriaService.registrar("UPDATE_USER", "users", updated.getId_usuario(),
    // null, updated.getUsername());

    return mapToResponse(updated);
  }

  public void changePassword(String username, ChangePasswordRequest request) {
    Usuario usuario = usuarioRepository.findByUsername(username)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

    if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPassword())) {
      throw new BadRequestException("Password actual incorrecto");
    }

    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new BadRequestException("Los passwords no coinciden");
    }

    usuario.setPassword(passwordEncoder.encode(request.getNewPassword()));
    usuarioRepository.save(usuario);

    // auditoriaService.registrar("CHANGE_PASSWORD", "users",
    // usuario.getId_usuario(), null, usuario.getUsername());
  }

  public void toggleEnabled(Long id) {
    Usuario usuario = usuarioRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

    usuario.setEnabled(!usuario.isEnabled());
    usuarioRepository.save(usuario);

    // auditoriaService.registrar(
    // usuario.getEnabled() ? "ENABLE_USER" : "DISABLE_USER",
    // "users",
    // usuario.getId_usuario(),
    // null,
    // usuario.getUsername());
  }

  @Transactional(readOnly = true)
  public UsuarioResponse findById(Long id) {
    Usuario usuario = usuarioRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    return mapToResponse(usuario);
  }

  @Transactional(readOnly = true)
  public List<UsuarioResponse> findAll() {
    return usuarioRepository.findAll().stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<UsuarioResponse> findByRoleName(String roleName) {
    String normalizedRole = roleName.startsWith("ROLE_")
        ? roleName
        : "ROLE_" + roleName;
    return usuarioRepository.findByRoleName(normalizedRole).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<UsuarioResponse> search(String searchTerm) {
    return usuarioRepository.searchUsuarios(searchTerm).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  private UsuarioResponse mapToResponse(Usuario usuario) {
    UsuarioResponse response = new UsuarioResponse();
    response.setId_usuario(usuario.getId_usuario());
    response.setUsername(usuario.getUsername());
    response.setEnabled(usuario.isEnabled());
    response.setPersona(personaService.findById(usuario.getPersona().getId_persona()));

    Set<RoleSimpleResponse> rolesResponse = usuario.getRoles().stream()
        .map(role -> {
          RoleSimpleResponse r = new RoleSimpleResponse();
          r.setId_role(role.getId_role());
          r.setName(role.getName());
          return r;
        })
        .collect(Collectors.toSet());
    response.setRoles(rolesResponse);

    return response;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Usuario> findByUsername(String username) {
    return usuarioRepository.findByUsername(username);
  }
  // @Override
  // @Transactional
  // public Optional<Usuario> update(Usuario user, Long id) {

  // Optional<Usuario> userOptional = this.findById(id);
  // return userOptional.map(existing -> {
  // existing.setUsername(user.getUsername());
  // // existing.setPassword(user.getPassword());

  // if (user.isEnabled() != null)
  // existing.setEnabled(user.isEnabled());
  // // existing.setEmail(user.getEmail());

  // // existing.setRoles(user.getRoles());
  // // existing.setRoles(getRoleOptional(user));

  // return Optional.of(userRepository.save(existing));
  // }).orElseGet(() -> Optional.empty());

  // }

  // @Override
  // @Transactional
  // public void delete(Long id) {
  // userRepository.deleteById(id);
  // }

  // private List<Role> getRoleOptional(Usuario user) {
  // List<Role> roles = new ArrayList<>();
  // Optional<Role> roleOptional = roleRepository.findByName("ROLE_USER");
  // roleOptional.ifPresent(roles::add);

  // if (user.isAdmin()) {
  // Optional<Role> adminRoleOptional = roleRepository.findByName("ROLE_ADMIN");
  // adminRoleOptional.ifPresent(roles::add);
  // }

  // return roles;
  // }

  // @Override
  // public boolean existsByUsername(String username) {
  // return userRepository.existsByUsername(username);
  // }
}
