package com.proyecto.fhce.library.dto.response;

import java.time.LocalDateTime;

import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;

public class AuditoriaResponse {
  private Long id_auditoria;
  private UsuarioSimpleResponse usuario;
  private String accion;
  private String tablaAfectada;
  private Long id_registro;
  private String valoresAnteriores;
  private String valoresNuevos;
  private LocalDateTime fechaHora;
  private String ipAddress;

  public Long getId_auditoria() {
    return id_auditoria;
  }

  public void setId_auditoria(Long id_auditoria) {
    this.id_auditoria = id_auditoria;
  }

  public UsuarioSimpleResponse getUsuario() {
    return usuario;
  }

  public void setUsuario(UsuarioSimpleResponse usuario) {
    this.usuario = usuario;
  }

  public String getAccion() {
    return accion;
  }

  public void setAccion(String accion) {
    this.accion = accion;
  }

  public String getTablaAfectada() {
    return tablaAfectada;
  }

  public void setTablaAfectada(String tablaAfectada) {
    this.tablaAfectada = tablaAfectada;
  }

  public Long getId_registro() {
    return id_registro;
  }

  public void setId_registro(Long id_registro) {
    this.id_registro = id_registro;
  }

  public String getValoresAnteriores() {
    return valoresAnteriores;
  }

  public void setValoresAnteriores(String valoresAnteriores) {
    this.valoresAnteriores = valoresAnteriores;
  }

  public String getValoresNuevos() {
    return valoresNuevos;
  }

  public void setValoresNuevos(String valoresNuevos) {
    this.valoresNuevos = valoresNuevos;
  }

  public LocalDateTime getFechaHora() {
    return fechaHora;
  }

  public void setFechaHora(LocalDateTime fechaHora) {
    this.fechaHora = fechaHora;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public void setIpAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }

}
