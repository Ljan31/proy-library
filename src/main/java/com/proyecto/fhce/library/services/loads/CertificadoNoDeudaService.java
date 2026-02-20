package com.proyecto.fhce.library.services.loads;

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;

public interface CertificadoNoDeudaService {
  public CertificadoResponse generar(CertificadoRequest request, Long bibliotecarioId);

  public ValidacionCertificadoResponse validar(String codigoVerificacion);

  public void actualizarCertificadosVencidos();
}
