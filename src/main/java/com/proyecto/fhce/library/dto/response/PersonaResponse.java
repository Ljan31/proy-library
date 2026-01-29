package com.proyecto.fhce.library.dto.response;

public class PersonaResponse {
  private Long id_persona;
  private String nombre;
  private String apellido_pat;
  private String apellido_mat;
  private String nombreCompleto; // computed
  private Integer ci;
  private String celular;
  private String email;

  public Long getId_persona() {
    return id_persona;
  }

  public void setId_persona(Long id_persona) {
    this.id_persona = id_persona;
  }

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

  public String getNombreCompleto() {
    return nombreCompleto;
  }

  public void setNombreCompleto(String nombreCompleto) {
    this.nombreCompleto = nombreCompleto;
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

}