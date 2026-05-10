package com.proyecto.fhce.library.dto.response.users;

public class EncargadoSimpleResponse {
  private Long idUsuario;
  private String username;
  private String nombreCompleto;
  private String rol;
  private String respaldoUrl;

  public Long getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(Long idUsuario) {
    this.idUsuario = idUsuario;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getNombreCompleto() {
    return nombreCompleto;
  }

  public void setNombreCompleto(String nombreCompleto) {
    this.nombreCompleto = nombreCompleto;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public String getRespaldoUrl() {
    return respaldoUrl;
  }

  public void setRespaldoUrl(String respaldoUrl) {
    this.respaldoUrl = respaldoUrl;
  }

}
