package com.proyecto.fhce.library.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.repositories.RoleRepository;

@Service
public class RoleServiceImpl implements RoleService {

  @Autowired
  private RoleRepository roleRepository;

  @Override
  @Transactional(readOnly = true)
  public List<Role> findAll() {
    return (List<Role>) roleRepository.findAll();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Role> findById(Long id) {
    return roleRepository.findById(id);
  }

  @Override
  public Optional<Role> findByName(String rol) {
    return roleRepository.findByName(rol);
  }

  @Override
  public Role save(Role rol) {
    return roleRepository.save(rol);
  }

  @Override
  public Optional<Role> update(Role rol, Long id) {

    Optional<Role> roleOptional = this.findById(id);
    return roleOptional.map(existing -> {
      if (rol.getName() != "")
        existing.setName(rol.getName());
      return Optional.of(roleRepository.save(existing));
    }).orElseGet(() -> Optional.empty());
  }

  @Override
  public void delete(Long id) {
    roleRepository.deleteById(id);

  }

}
