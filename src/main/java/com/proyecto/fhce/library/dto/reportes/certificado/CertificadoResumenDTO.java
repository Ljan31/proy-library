package com.proyecto.fhce.library.dto.reportes.certificado;

public class CertificadoResumenDTO {
  private Long totalEmitidos;
  private Long vigentes;
  private Long vencidos;
  private Long anulados;

  public Long getTotalEmitidos() {
    return totalEmitidos;
  }

  public void setTotalEmitidos(Long totalEmitidos) {
    this.totalEmitidos = totalEmitidos;
  }

  public Long getVigentes() {
    return vigentes;
  }

  public void setVigentes(Long vigentes) {
    this.vigentes = vigentes;
  }

  public Long getVencidos() {
    return vencidos;
  }

  public void setVencidos(Long vencidos) {
    this.vencidos = vencidos;
  }

  public Long getAnulados() {
    return anulados;
  }

  public void setAnulados(Long anulados) {
    this.anulados = anulados;
  }

}
