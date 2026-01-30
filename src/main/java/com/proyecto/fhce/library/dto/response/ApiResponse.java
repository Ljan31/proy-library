package com.proyecto.fhce.library.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public class ApiResponse<T> {
  private Boolean success;
  private String message;
  private T data;
  private LocalDateTime timestamp;
  private List<String> errors;

  public ApiResponse(Boolean success, String message, T data) {
    this.success = success;
    this.message = message;
    this.data = data;
    this.timestamp = LocalDateTime.now();
  }

  public void setErrors(List<String> errors) {
    this.errors = errors;
  }

  public static <T> ApiResponse<T> success(T data) {
    return new ApiResponse<>(true, "Operación exitosa", data);
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return new ApiResponse<>(true, message, data);
  }

  public static <T> ApiResponse<T> error(String message) {
    return new ApiResponse<>(false, message, null);
  }

  public static <T> ApiResponse<T> error(String message, List<String> errors) {
    ApiResponse<T> response = new ApiResponse<>(false, message, null);
    response.setErrors(errors);
    return response;
  }

  public Boolean getSuccess() {
    return success;
  }

  public void setSuccess(Boolean success) {
    this.success = success;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public List<String> getErrors() {
    return errors;
  }

}