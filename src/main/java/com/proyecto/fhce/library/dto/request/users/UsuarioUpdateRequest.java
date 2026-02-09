package com.proyecto.fhce.library.dto.request.users;

public class UsuarioUpdateRequest {
  private String nombre;
  private String apellido_pat;
  private String apellido_mat;
  private String celular;
  private String email;
  private String matricula;
  private Boolean enabled;

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

  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public String getMatricula() {
    return matricula;
  }

  public void setMatricula(String matricula) {
    this.matricula = matricula;
  }

}