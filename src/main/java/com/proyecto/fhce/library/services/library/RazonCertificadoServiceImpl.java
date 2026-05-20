package com.proyecto.fhce.library.services.library;

import com.proyecto.fhce.library.dto.request.library.RazonCertificadoRequestDTO;
import com.proyecto.fhce.library.dto.response.library.RazonCertificadoResponseDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.RazonCertificado;
import com.proyecto.fhce.library.mapper.RazonCertificadoMapper;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.RazonCertificadoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RazonCertificadoServiceImpl implements RazonCertificadoService {

  private final RazonCertificadoRepository razonRepository;
  private final BibliotecaRepository bibliotecaRepository;

  public RazonCertificadoServiceImpl(
      RazonCertificadoRepository razonRepository,
      BibliotecaRepository bibliotecaRepository) {
    this.razonRepository = razonRepository;
    this.bibliotecaRepository = bibliotecaRepository;
  }

  @Override
  public RazonCertificadoResponseDTO crear(RazonCertificadoRequestDTO dto) {

    Biblioteca biblioteca = bibliotecaRepository.findById(dto.getBibliotecaId())
        .orElseThrow(() -> new RuntimeException("Biblioteca no encontrada"));

    RazonCertificado entity = RazonCertificadoMapper.toEntity(dto, biblioteca);

    RazonCertificado saved = razonRepository.save(entity);

    return RazonCertificadoMapper.toDTO(saved);
  }

  @Override
  public List<RazonCertificadoResponseDTO> listarTodas() {
    return razonRepository.findAll()
        .stream()
        .map(RazonCertificadoMapper::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public List<RazonCertificadoResponseDTO> listarPorBiblioteca(Long bibliotecaId) {
    return razonRepository.findByBibliotecaIdBiblioteca(bibliotecaId)
        .stream()
        .map(RazonCertificadoMapper::toDTO)
        .collect(Collectors.toList());
  }

  @Override
  public RazonCertificadoResponseDTO obtenerPorId(Long id) {
    RazonCertificado entity = razonRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Razón no encontrada"));

    return RazonCertificadoMapper.toDTO(entity);
  }

  @Override
  public RazonCertificadoResponseDTO actualizar(Long id, RazonCertificadoRequestDTO dto) {

    RazonCertificado entity = razonRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Razón no encontrada"));

    entity.setNombre(dto.getNombre());
    entity.setDescripcion(dto.getDescripcion());
    entity.setActivo(dto.getActivo());

    // actualizar requisitos JSON
    entity.setRequisitos(
        RazonCertificadoMapper.toEntity(dto, entity.getBiblioteca()).getRequisitos());

    RazonCertificado updated = razonRepository.save(entity);

    return RazonCertificadoMapper.toDTO(updated);
  }

  @Override
  public void eliminar(Long id) {
    if (!razonRepository.existsById(id)) {
      throw new RuntimeException("Razón no encontrada");
    }
    razonRepository.deleteById(id);
  }
}