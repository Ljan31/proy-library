package com.proyecto.fhce.library.mapper;

import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.dto.ReglasPrestamoDTO;
import com.proyecto.fhce.library.dto.request.loads.ConfiguracionPrestamoRequestDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionPrestamoResponseDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;

@Component
public class ConfiguracionPrestamoMapper {

  public ConfiguracionPrestamo toEntity(ConfiguracionPrestamoRequestDTO dto,
      Biblioteca biblioteca) {
    ConfiguracionPrestamo entity = new ConfiguracionPrestamo();
    entity.setBiblioteca(biblioteca);
    aplicarCampos(entity, dto);
    return entity;
  }

  public void actualizarEntidad(ConfiguracionPrestamo entity,
      ConfiguracionPrestamoRequestDTO dto,
      Biblioteca biblioteca) {
    entity.setBiblioteca(biblioteca);
    aplicarCampos(entity, dto);
  }

  public ConfiguracionPrestamoResponseDTO toResponseDTO(ConfiguracionPrestamo entity) {
    ConfiguracionPrestamoResponseDTO dto = new ConfiguracionPrestamoResponseDTO();
    dto.setIdConfig(entity.getIdConfig());
    dto.setDiasPrestamoMax(entity.getDiasPrestamoMax());
    dto.setEjemplaresMaxDomicilio(entity.getEjemplaresMaxDomicilio());
    dto.setEjemplaresMaxSala(entity.getEjemplaresMaxSala());
    dto.setMultaPorDia(entity.getMultaPorDia());
    dto.setMultaMaxDias(entity.getMultaMaxDias());
    dto.setDiasSuspension(entity.getDiasSuspension());
    dto.setDiasReserva(entity.getDiasReserva());
    dto.setRenovacionesMax(entity.getRenovacionesMax());

    if (entity.getBiblioteca() != null) {
      dto.setBibliotecaId(entity.getBiblioteca().getIdBiblioteca());
      dto.setNombreBiblioteca(entity.getBiblioteca().getNombre());
    }

    return dto;
  }

  public ReglasPrestamoDTO toReglasDTO(ConfiguracionPrestamo entity,
      Integer ejemplaresPermitidos) {
    ReglasPrestamoDTO dto = new ReglasPrestamoDTO();
    dto.setIdConfig(entity.getIdConfig());
    dto.setDiasPrestamoMax(entity.getDiasPrestamoMax());
    dto.setRenovacionesMax(entity.getRenovacionesMax());
    dto.setEjemplaresPermitidos(ejemplaresPermitidos);
    dto.setMultaPorDia(entity.getMultaPorDia());
    dto.setMultaMaxDias(entity.getMultaMaxDias());
    dto.setDiasSuspension(entity.getDiasSuspension());
    dto.setDiasReserva(entity.getDiasReserva());
    return dto;
  }

  // ── Método privado: evita repetir la asignación de campos ──
  private void aplicarCampos(ConfiguracionPrestamo entity,
      ConfiguracionPrestamoRequestDTO dto) {
    entity.setDiasPrestamoMax(dto.getDiasPrestamoMax());
    entity.setRenovacionesMax(dto.getRenovacionesMax());
    entity.setEjemplaresMaxDomicilio(dto.getEjemplaresMaxDomicilio());
    entity.setMultaPorDia(dto.getMultaPorDia());
    entity.setMultaMaxDias(dto.getMultaMaxDias());
    entity.setDiasSuspension(dto.getDiasSuspension());
    entity.setDiasReserva(dto.getDiasReserva());
    entity.setEjemplaresMaxSala(dto.getEjemplaresMaxSala());
  }
}