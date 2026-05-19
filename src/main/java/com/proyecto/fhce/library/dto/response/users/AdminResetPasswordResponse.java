package com.proyecto.fhce.library.dto.response.users;

public class AdminResetPasswordResponse {

  private String temporaryPassword;

  public AdminResetPasswordResponse() {
  }

  public AdminResetPasswordResponse(String temporaryPassword) {
    this.temporaryPassword = temporaryPassword;
  }

  public String getTemporaryPassword() {
    return temporaryPassword;
  }

  public void setTemporaryPassword(String temporaryPassword) {
    this.temporaryPassword = temporaryPassword;
  }
}