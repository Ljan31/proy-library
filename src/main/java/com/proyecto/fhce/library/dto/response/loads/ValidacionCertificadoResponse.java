package com.proyecto.fhce.library.dto.response.loads;

public class ValidacionCertificadoResponse {
  private Boolean valido;
  private String mensaje;
  private CertificadoResponse certificado;

  public Boolean getValido() {
    return valido;
  }

  public void setValido(Boolean valido) {
    this.valido = valido;
  }

  public String getMensaje() {
    return mensaje;
  }

  public void setMensaje(String mensaje) {
    this.mensaje = mensaje;
  }

  public CertificadoResponse getCertificado() {
    return certificado;
  }

  public void setCertificado(CertificadoResponse certificado) {
    this.certificado = certificado;
  }

}
