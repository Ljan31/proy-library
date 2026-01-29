package com.proyecto.fhce.library.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
  @NotBlank(message = "Username es requerido")
  @Size(min = 4, max = 50, message = "Username debe tener entre 4 y 50 caracteres")
  private String username;

  @NotBlank(message = "Password es requerido")
  @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
  private String password;

  @NotNull(message = "Datos de persona son requeridos")
  private PersonaRequest persona;

  private Set<Long> roleIds;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public PersonaRequest getPersona() {
    return persona;
  }

  public void setPersona(PersonaRequest persona) {
    this.persona = persona;
  }

  public Set<Long> getRoleIds() {
    return roleIds;
  }

  public void setRoleIds(Set<Long> roleIds) {
    this.roleIds = roleIds;
  }

}