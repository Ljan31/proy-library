package com.proyecto.fhce.library.dto.request.users;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {
  @NotBlank(message = "Password actual es requerido")
  private String currentPassword;

  @NotBlank(message = "Nuevo password es requerido")
  @Size(min = 6, message = "Password debe tener al menos 6 caracteres")
  private String newPassword;

  @NotBlank(message = "Confirmación de password es requerida")
  private String confirmPassword;

  public String getCurrentPassword() {
    return currentPassword;
  }

  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  public String getNewPassword() {
    return newPassword;
  }

  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }

}