package com.proyecto.fhce.library.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.services.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

  @Autowired
  private RoleService roleService;

  @GetMapping
  public List<Role> findAll() {
    return roleService.findAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<Role> findById(@PathVariable Long id) {
    return roleService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/name/{name}")
  public ResponseEntity<Role> findByName(@PathVariable String name) {
    return roleService.findByName(name)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public ResponseEntity<Role> create(@RequestBody Role rol) {
    return new ResponseEntity<>(roleService.save(rol), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Role rol) {
    // return new ResponseEntity<>(userService.update(user, id), HttpStatus.OK);
    Optional<Role> roleOptional = roleService.update(rol, id);

    return roleOptional.map(roleUpdated -> ResponseEntity.status(HttpStatus.OK).body(roleUpdated))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    if (roleService.findById(id).isPresent()) {
      roleService.delete(id);
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.notFound().build();
  }
}
