package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.constraints.NotNull;

public class CertificadoGenerarRequest {

  @NotNull
  private Long usuarioId;

  @NotNull
  private Long bibliotecaId;
  private String formato = "docx";

  // Si false, genera pero no persiste el PDF
  private boolean guardarPdf = false;

  public Long getUsuarioId() {
    return usuarioId;
  }

  public void setUsuarioId(Long usuarioId) {
    this.usuarioId = usuarioId;
  }

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public String getFormato() {
    return formato;
  }

  public void setFormato(String formato) {
    this.formato = formato;
  }

  public boolean isGuardarPdf() {
    return guardarPdf;
  }

  public void setGuardarPdf(boolean guardarPdf) {
    this.guardarPdf = guardarPdf;
  }

}