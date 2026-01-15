package com.proyecto.fhce.library.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.RoleRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private RoleRepository roleRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public List<Usuario> findAll() {
    return (List<Usuario>) userRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Usuario> findById(Long id) {
    return userRepository.findById(id);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Usuario> findByUsername(String username) {
    return userRepository.findByUsername(username);
  }

  @Override
  @Transactional
  public Usuario save(Usuario user) {
    user.setRoles(getRoleOptional(user));
    user.setEnabled(true);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    return userRepository.save(user);
  }

  @Override
  @Transactional
  public Optional<Usuario> update(Usuario user, Long id) {

    Optional<Usuario> userOptional = this.findById(id);
    return userOptional.map(existing -> {
      existing.setUsername(user.getUsername());
      // existing.setPassword(user.getPassword());

      if (user.isEnabled() != null)
        existing.setEnabled(user.isEnabled());
      // existing.setEmail(user.getEmail());

      // existing.setRoles(user.getRoles());
      existing.setRoles(getRoleOptional(user));

      return Optional.of(userRepository.save(existing));
    }).orElseGet(() -> Optional.empty());

  }

  @Override
  @Transactional
  public void delete(Long id) {
    userRepository.deleteById(id);
  }

  private List<Role> getRoleOptional(Usuario user) {
    List<Role> roles = new ArrayList<>();
    Optional<Role> roleOptional = roleRepository.findByName("ROLE_USER");
    roleOptional.ifPresent(roles::add);

    if (user.isAdmin()) {
      Optional<Role> adminRoleOptional = roleRepository.findByName("ROLE_ADMIN");
      adminRoleOptional.ifPresent(roles::add);
    }

    return roles;
  }

  @Override
  public boolean existsByUsername(String username) {
    return userRepository.existsByUsername(username);
  }
}
