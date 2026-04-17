package com.proyecto.fhce.library.services.loads;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import com.proyecto.fhce.library.dto.request.loads.CertificadoRequest;
import com.proyecto.fhce.library.dto.response.loads.CertificadoResponse;
import com.proyecto.fhce.library.dto.response.loads.ValidacionCertificadoResponse;
import com.proyecto.fhce.library.enums.EstadoCertificado;

public interface CertificadoNoDeudaService {
  public CertificadoResponse findById(Long id);

  public List<CertificadoResponse> findByUsuario(Long usuarioId, Long solicitanteId,
      Long bibliotecaId, Collection<? extends GrantedAuthority> authorities);

  public CertificadoResponse findByIdConAutorizacion(
      Long id,
      Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities);

  public CertificadoResponse generar(CertificadoRequest request, Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities);

  public List<CertificadoResponse> findByBiblioteca(Long bibliotecaId, EstadoCertificado estado,
      Long solicitanteId, Collection<? extends GrantedAuthority> authorities);

  public CertificadoResponse anular(Long id, Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities);

  public ValidacionCertificadoResponse validar(String codigoVerificacion);

  public void actualizarCertificadosVencidos();
}
