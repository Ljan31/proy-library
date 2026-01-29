package com.proyecto.fhce.library.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class PersonaRequest {
  @NotBlank(message = "Nombre es requerido")
  private String nombre;

  @NotBlank(message = "Apellido paterno es requerido")
  private String apellido_pat;

  private String apellido_mat;

  @NotNull(message = "CI es requerido")
  @Min(value = 1, message = "CI debe ser mayor a 0")
  private Integer ci;

  @Pattern(regexp = "^[0-9]{7,8}$", message = "Celular debe tener 7-8 dígitos")
  private String celular;

  @Email(message = "Email debe ser válido")
  private String email;

}