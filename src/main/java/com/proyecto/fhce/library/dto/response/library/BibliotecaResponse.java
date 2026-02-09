package com.proyecto.fhce.library.dto.response.library;

import com.proyecto.fhce.library.dto.response.CarreraSimpleResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.enums.EstadoBiblioteca;
import com.proyecto.fhce.library.enums.TipoBiblioteca;

public class BibliotecaResponse {
  private Long id_biblioteca;
  private String nombre;
  private TipoBiblioteca tipoBiblioteca;
  private CarreraSimpleResponse carrera;
  private String direccion;
  private String telefono;
  private String email;
  private String horario_atencion;
  private UsuarioSimpleResponse encargado;
  private EstadoBiblioteca estado;
  private Integer ejemplaresTotal;
  private Integer ejemplaresDisponibles;

  public Long getId_biblioteca() {
    return id_biblioteca;
  }

  public void setId_biblioteca(Long id_biblioteca) {
    this.id_biblioteca = id_biblioteca;
  }

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

  public CarreraSimpleResponse getCarrera() {
    return carrera;
  }

  public void setCarrera(CarreraSimpleResponse carrera) {
    this.carrera = carrera;
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

  public UsuarioSimpleResponse getEncargado() {
    return encargado;
  }

  public void setEncargado(UsuarioSimpleResponse encargado) {
    this.encargado = encargado;
  }

  public EstadoBiblioteca getEstado() {
    return estado;
  }

  public void setEstado(EstadoBiblioteca estado) {
    this.estado = estado;
  }

  public Integer getEjemplaresTotal() {
    return ejemplaresTotal;
  }

  public void setEjemplaresTotal(Integer ejemplaresTotal) {
    this.ejemplaresTotal = ejemplaresTotal;
  }

  public Integer getEjemplaresDisponibles() {
    return ejemplaresDisponibles;
  }

  public void setEjemplaresDisponibles(Integer ejemplaresDisponibles) {
    this.ejemplaresDisponibles = ejemplaresDisponibles;
  }

}
