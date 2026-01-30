package com.proyecto.fhce.library.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
  @NotBlank(message = "Username es requerido")
  private String username;

  @NotBlank(message = "Password es requerido")
  private String password;

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

}
