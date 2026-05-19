package com.proyecto.fhce.library.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.fhce.library.dto.request.library.RazonCertificadoRequestDTO;
import com.proyecto.fhce.library.dto.response.library.RazonCertificadoResponseDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.RazonCertificado;

import java.util.List;

public class RazonCertificadoMapper {

  private static final ObjectMapper mapper = new ObjectMapper();

  // ENTITY -> DTO
  public static RazonCertificadoResponseDTO toDTO(RazonCertificado entity) {
    RazonCertificadoResponseDTO dto = new RazonCertificadoResponseDTO();

    dto.setIdRazon(entity.getIdRazon());
    dto.setNombre(entity.getNombre());
    dto.setDescripcion(entity.getDescripcion());
    dto.setRequisitos(entity.getRequisitos());
    dto.setActivo(entity.getActivo());

    if (entity.getBiblioteca() != null) {
      dto.setBibliotecaId(entity.getBiblioteca().getIdBiblioteca());
    }

    return dto;
  }

  // DTO -> ENTITY
  public static RazonCertificado toEntity(
      RazonCertificadoRequestDTO dto,
      Biblioteca biblioteca) {
    RazonCertificado entity = new RazonCertificado();

    entity.setNombre(dto.getNombre());
    entity.setDescripcion(dto.getDescripcion());
    entity.setActivo(dto.getActivo());

    entity.setBiblioteca(biblioteca);

    try {
      entity.setRequisitos(
          mapper.writeValueAsString(dto.getRequisitos()));
    } catch (Exception e) {
      throw new RuntimeException("Error converting requisitos to JSON", e);
    }

    return entity;
  }

  // JSON -> List (helper opcional)
  public static List<String> parseRequisitos(String json) {
    try {
      return mapper.readValue(json, new TypeReference<List<String>>() {
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}