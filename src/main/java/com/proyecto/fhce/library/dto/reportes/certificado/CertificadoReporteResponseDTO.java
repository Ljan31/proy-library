package com.proyecto.fhce.library.dto.reportes.certificado;

import java.util.List;

public class CertificadoReporteResponseDTO {
  private CertificadoResumenDTO resumen;
  private List<CertificadoReporteItemDTO> certificados;

  public CertificadoResumenDTO getResumen() {
    return resumen;
  }

  public void setResumen(CertificadoResumenDTO resumen) {
    this.resumen = resumen;
  }

  public List<CertificadoReporteItemDTO> getCertificados() {
    return certificados;
  }

  public void setCertificados(List<CertificadoReporteItemDTO> certificados) {
    this.certificados = certificados;
  }

}