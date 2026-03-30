package com.proyecto.fhce.library.services.loads;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;
import com.proyecto.fhce.library.entities.CertificadoNoDeuda;

public interface CertificadoNoDeudaService {
  public CertificadoResponse findById(Long id);

  public List<CertificadoResponse> findByUsuario(
      Long requesterId,
      Long usuarioId,
      Collection<? extends GrantedAuthority> authorities);

  public CertificadoResponse generar(CertificadoRequest request, Long bibliotecarioId);

  public ValidacionCertificadoResponse validar(String codigoVerificacion);

  public void actualizarCertificadosVencidos();
}
