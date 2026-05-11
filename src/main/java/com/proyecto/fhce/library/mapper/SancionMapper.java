package com.proyecto.fhce.library.mapper;

import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.dto.SancionDTO.SancionResponseDTO;
import com.proyecto.fhce.library.dto.SancionDTO.SancionResumenDTO;
import com.proyecto.fhce.library.entities.Sancion;

import java.util.List;

@Component
public class SancionMapper {

  public SancionResponseDTO toResponseDTO(Sancion s) {
    String nombreUsuario = null;
    String ciUsuario = null;

    if (s.getUsuario() != null && s.getUsuario().getPersona() != null) {
      var persona = s.getUsuario().getPersona();
      nombreUsuario = persona.getNombre() + " " + persona.getApellido_pat();
      ciUsuario = persona.getCi() + "";
    }

    return new SancionResponseDTO(
        s.getIdSancion(),
        s.getUsuario() != null ? s.getUsuario().getId_usuario() : null,
        nombreUsuario,
        ciUsuario,
        s.getPrestamo() != null ? s.getPrestamo().getIdPrestamo() : null,
        s.getBiblioteca() != null ? s.getBiblioteca().getIdBiblioteca() : null,
        s.getBiblioteca() != null ? s.getBiblioteca().getNombre() : null,
        s.getTipoSancion(),
        s.getMotivo(),
        s.getEstado(),
        s.getDiasRetraso(),
        s.getMontoMulta(),
        s.getDiasSuspension(),
        s.getFechaGeneracion(),
        s.getFechaInicioSuspension(),
        s.getFechaFinSuspension(),
        s.getFechaPago(),
        s.getFechaCondonacion(),
        s.getMetodoPago(),
        s.getObservaciones(),
        s.suspensionVigente());
  }

  public SancionResumenDTO toResumenDTO(Sancion s) {
    return new SancionResumenDTO(
        s.getIdSancion(),
        s.getPrestamo() != null ? s.getPrestamo().getIdPrestamo() : null,
        s.getTipoSancion(),
        s.getEstado(),
        s.getMontoMulta(),
        s.getFechaFinSuspension(),
        s.suspensionVigente(),
        s.getMotivo() != null ? s.getMotivo().name() : null);
  }

  public List<SancionResponseDTO> toResponseDTOList(List<Sancion> sanciones) {
    return sanciones.stream().map(this::toResponseDTO).toList();
  }
}