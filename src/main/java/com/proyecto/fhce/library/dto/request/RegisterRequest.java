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
}