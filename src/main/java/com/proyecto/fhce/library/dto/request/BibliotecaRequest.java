package com.proyecto.fhce.library.dto.request;

import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class BibliotecaRequest {
  @NotBlank(message = "Nombre de biblioteca es requerido")
  @Size(max = 200)
  private String nombre;

  @NotNull(message = "Tipo de biblioteca es requerido")
  private TipoBiblioteca tipoBiblioteca;

  private Long carreraId;

  private String direccion;

  @Pattern(regexp = "^[0-9-]{7,20}$", message = "Teléfono inválido")
  private String telefono;

  @Email(message = "Email inválido")
  private String email;

  private String horario_atencion;

  private Long encargadoId;

  private EstadoBiblioteca estado;

  public String getNombre() {
    return nombre;
  }

  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  public TipoBiblioteca getTipoBiblioteca() {
    return tipoBiblioteca;
  }

  public void setTipoBiblioteca(TipoBiblioteca tipoBiblioteca) {
    this.tipoBiblioteca = tipoBiblioteca;
  }

  public Long getCarreraId() {
    return carreraId;
  }

  public void setCarreraId(Long carreraId) {
    this.carreraId = carreraId;
  }

  public String getDireccion() {
    return direccion;
  }

  public void setDireccion(String direccion) {
    this.direccion = direccion;
  }

  public String getTelefono() {
    return telefono;
  }

  public void setTelefono(String telefono) {
    this.telefono = telefono;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getHorario_atencion() {
    return horario_atencion;
  }

  public void setHorario_atencion(String horario_atencion) {
    this.horario_atencion = horario_atencion;
  }

  public Long getEncargadoId() {
    return encargadoId;
  }

  public void setEncargadoId(Long encargadoId) {
    this.encargadoId = encargadoId;
  }

  public EstadoBiblioteca getEstado() {
    return estado;
  }

  public void setEstado(EstadoBiblioteca estado) {
    this.estado = estado;
  }

}
