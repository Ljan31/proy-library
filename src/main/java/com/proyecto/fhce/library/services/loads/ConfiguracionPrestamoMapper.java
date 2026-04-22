package com.proyecto.fhce.library.services.loads;

import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.dto.request.loads.ConfiguracionPrestamoRequestDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionPrestamoResponseDTO;
import com.proyecto.fhce.library.dto.response.loads.ConfiguracionResueltaDTO;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.ConfiguracionPrestamo;
import com.proyecto.fhce.library.entities.Role;
import com.proyecto.fhce.library.enums.TipoPrestamo;

@Component
public class ConfiguracionPrestamoMapper {

  public ConfiguracionPrestamo toEntity(ConfiguracionPrestamoRequestDTO dto,
      Biblioteca biblioteca, Role rol) {
    ConfiguracionPrestamo entity = new ConfiguracionPrestamo();
    entity.setBiblioteca(biblioteca);
    entity.setRol(rol);
    entity.setTipoPrestamo(dto.getTipoPrestamo());
    entity.setDiasPrestamoMax(dto.getDiasPrestamoMax());
    entity.setEjemplaresMaxDomicilio(dto.getEjemplaresMaxDomicilio());
    entity.setEjemplaresMaxSala(dto.getEjemplaresMaxSala());
    entity.setMultaPorDia(dto.getMultaPorDia());
    entity.setMultaMaxDias(dto.getMultaMaxDias());
    entity.setDiasSuspension(dto.getDiasSuspension());
    entity.setDiasReserva(dto.getDiasReserva());
    return entity;
  }

  public void actualizarEntidad(ConfiguracionPrestamo entity,
      ConfiguracionPrestamoRequestDTO dto,
      Biblioteca biblioteca, Role rol) {
    entity.setBiblioteca(biblioteca);
    entity.setRol(rol);
    entity.setTipoPrestamo(dto.getTipoPrestamo());
    entity.setDiasPrestamoMax(dto.getDiasPrestamoMax());
    entity.setEjemplaresMaxDomicilio(dto.getEjemplaresMaxDomicilio());
    entity.setEjemplaresMaxSala(dto.getEjemplaresMaxSala());
    entity.setMultaPorDia(dto.getMultaPorDia());
    entity.setMultaMaxDias(dto.getMultaMaxDias());
    entity.setDiasSuspension(dto.getDiasSuspension());
    entity.setDiasReserva(dto.getDiasReserva());
  }

  public ConfiguracionPrestamoResponseDTO toResponseDTO(ConfiguracionPrestamo entity) {
    ConfiguracionPrestamoResponseDTO dto = new ConfiguracionPrestamoResponseDTO();
    dto.setIdConfig(entity.getIdConfig());
    dto.setTipoPrestamo(entity.getTipoPrestamo());
    dto.setDiasPrestamoMax(entity.getDiasPrestamoMax());
    dto.setEjemplaresMaxDomicilio(entity.getEjemplaresMaxDomicilio());
    dto.setEjemplaresMaxSala(entity.getEjemplaresMaxSala());
    dto.setMultaPorDia(entity.getMultaPorDia());
    dto.setMultaMaxDias(entity.getMultaMaxDias());
    dto.setDiasSuspension(entity.getDiasSuspension());
    dto.setDiasReserva(entity.getDiasReserva());

    if (entity.getBiblioteca() != null) {
      dto.setBibliotecaId(entity.getBiblioteca().getIdBiblioteca());
      dto.setNombreBiblioteca(entity.getBiblioteca().getNombre());
    }
    if (entity.getRol() != null) {
      dto.setRolId(entity.getRol().getId_role());
      dto.setNombreRol(entity.getRol().getName());
    }
    return dto;
  }

  public ConfiguracionResueltaDTO toResueltaDTO(ConfiguracionPrestamo entity) {
    ConfiguracionResueltaDTO dto = new ConfiguracionResueltaDTO();
    dto.setIdConfig(entity.getIdConfig());
    dto.setTipoPrestamo(entity.getTipoPrestamo());
    dto.setDiasPrestamoMax(entity.getDiasPrestamoMax());
    dto.setMultaPorDia(entity.getMultaPorDia());
    dto.setMultaMaxDias(entity.getMultaMaxDias());
    dto.setDiasSuspension(entity.getDiasSuspension());
    dto.setDiasReserva(entity.getDiasReserva());

    boolean tieneBiblioteca = entity.getBiblioteca() != null;
    boolean tieneRol = entity.getRol() != null;

    if (tieneBiblioteca && tieneRol) {
      dto.setNivelAplicacion("ROL_BIBLIOTECA");
      dto.setEjemplaresPermitidos(
          entity.getTipoPrestamo() == TipoPrestamo.DOMICILIO
              ? entity.getEjemplaresMaxDomicilio()
              : entity.getEjemplaresMaxSala());
    } else if (tieneBiblioteca) {
      dto.setNivelAplicacion("BIBLIOTECA");
    } else {
      dto.setNivelAplicacion("GLOBAL");
    }
    return dto;
  }
}