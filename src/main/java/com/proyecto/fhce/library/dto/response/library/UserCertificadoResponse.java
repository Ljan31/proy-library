package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

public class UserCertificadoResponse {
  private Long usuarioId;
  private String nombreCompleto;
  private String ci;
  private Long bibliotecaId;
  private String nombreBiblioteca;
  private boolean tieneDeuda;
  private String grado;
  private String responsable;
  private String codigoVerificacion;
  private List<MaterialPendienteDTO> materialesPendientes;

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public String getNombreCompleto() {
    return nombreCompleto;
  }

  public void setNombreCompleto(String nombreCompleto) {
    this.nombreCompleto = nombreCompleto;
  }

  public String getCi() {
    return ci;
  }

  public void setCi(String ci) {
    this.ci = ci;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getNombreBiblioteca() {
    return nombreBiblioteca;
  }

  public void setNombreBiblioteca(String nombreBiblioteca) {
    this.nombreBiblioteca = nombreBiblioteca;
  }

  public boolean isTieneDeuda() {
    return tieneDeuda;
  }

  public void setTieneDeuda(boolean tieneDeuda) {
    this.tieneDeuda = tieneDeuda;
  }

  public List<MaterialPendienteDTO> getMaterialesPendientes() {
    return materialesPendientes;
  }

  public void setMaterialesPendientes(List<MaterialPendienteDTO> materialesPendientes) {
    this.materialesPendientes = materialesPendientes;
  }

  public String getGrado() {
    return grado;
  }

  public void setGrado(String grado) {
    this.grado = grado;
  }

  public String getResponsable() {
    return responsable;
  }

  public void setResponsable(String responsable) {
    this.responsable = responsable;
  }

  public String getCodigoVerificacion() {
    return codigoVerificacion;
  }

  public void setCodigoVerificacion(String codigoVerificacion) {
    this.codigoVerificacion = codigoVerificacion;
  }

}
