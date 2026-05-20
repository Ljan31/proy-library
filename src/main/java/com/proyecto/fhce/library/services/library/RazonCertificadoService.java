package com.proyecto.fhce.library.services.library;

import java.util.List;

import com.proyecto.fhce.library.dto.request.library.RazonCertificadoRequestDTO;
import com.proyecto.fhce.library.dto.response.library.RazonCertificadoResponseDTO;

public interface RazonCertificadoService {

  RazonCertificadoResponseDTO crear(RazonCertificadoRequestDTO dto);

  List<RazonCertificadoResponseDTO> listarTodas();

  List<RazonCertificadoResponseDTO> listarPorBiblioteca(Long bibliotecaId);

  RazonCertificadoResponseDTO obtenerPorId(Long id);

  RazonCertificadoResponseDTO actualizar(Long id, RazonCertificadoRequestDTO dto);

  void eliminar(Long id);
}