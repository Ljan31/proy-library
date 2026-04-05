package com.proyecto.fhce.library.dto.response.users;

import java.util.Optional;
import java.util.Set;

import com.proyecto.fhce.library.dto.response.library.BibliotecaResponse;

public class UsuarioResponse {
  private Long id_usuario;
  private String username;
  private Boolean enabled;
  private Integer intentosLogin;
  private PersonaResponse persona;
  private Set<RoleSimpleResponse> roles;
  private Optional<BibliotecaResponse> biblioteca;
  private Set<UsuarioCarreraResponse> userCarrera;

  // private List<CarreraSimpleResponse> carreras;
  // private LocalDateTime fechaRegistro;
  public Long getId_usuario() {
    return id_usuario;
  }

  public void setId_usuario(Long id_usuario) {
    this.id_usuario = id_usuario;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public Integer getIntentosLogin() {
    return intentosLogin;
  }

  public void setIntentosLogin(Integer intentosLogin) {
    this.intentosLogin = intentosLogin;
  }

  public PersonaResponse getPersona() {
    return persona;
  }

  public void setPersona(PersonaResponse persona) {
    this.persona = persona;
  }

  public Set<RoleSimpleResponse> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleSimpleResponse> roles) {
    this.roles = roles;
  }

  public Optional<BibliotecaResponse> getBiblioteca() {
    return biblioteca;
  }

  public void setBiblioteca(Optional<BibliotecaResponse> biblioteca) {
    this.biblioteca = biblioteca;
  }

  public Set<UsuarioCarreraResponse> getUserCarrera() {
    return userCarrera;
  }

  public void setUserCarrera(Set<UsuarioCarreraResponse> userCarrera) {
    this.userCarrera = userCarrera;
  }

}