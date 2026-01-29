package com.proyecto.fhce.library.dto.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RoleRequest {
  @NotBlank(message = "Nombre del rol es requerido")
  @Size(max = 50)
  private String name;

  private Set<Long> permisoIds;
}