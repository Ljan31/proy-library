package com.proyecto.fhce.library.services.library;

import com.proyecto.fhce.library.dto.request.library.RazonCertificadoRequestDTO;
import com.proyecto.fhce.library.dto.response.library.RazonCertificadoResponseDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.RazonCertificado;
import com.proyecto.fhce.library.mapper.RazonCertificadoMapper;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.RazonCertificadoRepository;
import com.proyecto.fhce.library.services.AuditoriaService;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RazonCertificadoServiceImpl implements RazonCertificadoService {

  private final RazonCertificadoRepository razonRepository;
  private final BibliotecaRepository bibliotecaRepository;
  private final AuditoriaService auditoriaService;

  public RazonCertificadoServiceImpl(
      RazonCertificadoRepository razonRepository,
      BibliotecaRepository bibliotecaRepository, AuditoriaService auditoriaService) {
    this.razonRepository = razonRepository;
    this.bibliotecaRepository = bibliotecaRepository;
    this.auditoriaService = auditoriaService;
  }

  @Override
  public RazonCertificadoResponseDTO crear(RazonCertificadoRequestDTO dto) {

    Biblioteca biblioteca = bibliotecaRepository.findById(dto.getBibliotecaId())
        .orElseThrow(() -> new RuntimeException("Biblioteca no encontrada"));

    RazonCertificado entity = RazonCertificadoMapper.toEntity(dto, biblioteca);

    RazonCertificado saved = razonRepository.save(entity);

    auditoriaService.registrar(
        "CREAR_RAZON_CERTIFICADO",
        "RazonCertificado",
        saved.getIdRazon(),
        null,
        mapToAuditoria(saved));

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
    return razonRepository.findByBiblioteca_IdBiblioteca(bibliotecaId)
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
    Map<String, Object> anterior = mapToAuditoria(entity);

    entity.setNombre(dto.getNombre());
    entity.setDescripcion(dto.getDescripcion());
    entity.setActivo(dto.getActivo());

    // actualizar requisitos JSON
    entity.setRequisitos(
        RazonCertificadoMapper.toEntity(dto, entity.getBiblioteca()).getRequisitos());

    RazonCertificado updated = razonRepository.save(entity);

    auditoriaService.registrar(
        "ACTUALIZAR_RAZON_CERTIFICADO",
        "RazonCertificado",
        updated.getIdRazon(),
        anterior,
        mapToAuditoria(updated));

    return RazonCertificadoMapper.toDTO(updated);
  }

  @Override
  public void eliminar(Long id) {
    RazonCertificado entity = razonRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Razón no encontrada"));
    Map<String, Object> anterior = mapToAuditoria(entity);

    razonRepository.delete(entity);

    auditoriaService.registrar(
        "ELIMINAR_RAZON_CERTIFICADO",
        "RazonCertificado",
        id,
        anterior,
        null);
  }

  private Map<String, Object> mapToAuditoria(RazonCertificado razon) {
    Map<String, Object> datos = new LinkedHashMap<>();

    datos.put("idRazon", razon.getIdRazon());
    datos.put("nombre", razon.getNombre());
    datos.put("descripcion", razon.getDescripcion());
    datos.put("activo", razon.getActivo());
    datos.put("requisitos", razon.getRequisitos());

    datos.put("bibliotecaId", razon.getBiblioteca() != null
        ? razon.getBiblioteca().getIdBiblioteca()
        : null);

    return datos;
  }
}