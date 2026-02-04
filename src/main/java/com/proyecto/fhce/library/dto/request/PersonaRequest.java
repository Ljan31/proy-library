package com.proyecto.fhce.library.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Datos de persona")
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

  private String matricula;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public String getApellido_pat() {
    return apellido_pat;
  }

  public void setApellido_pat(String apellido_pat) {
    this.apellido_pat = apellido_pat;
  }

  public String getApellido_mat() {
    return apellido_mat;
  }

  public void setApellido_mat(String apellido_mat) {
    this.apellido_mat = apellido_mat;
  }

  public Integer getCi() {
    return ci;
  }

  public void setCi(Integer ci) {
    this.ci = ci;
  }

  public String getCelular() {
    return celular;
  }

  public void setCelular(String celular) {
    this.celular = celular;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getMatricula() {
    return matricula;
  }

  public void setMatricula(String matricula) {
    this.matricula = matricula;
  }

}