package com.proyecto.fhce.library.mapper;

import org.springframework.stereotype.Component;

import com.proyecto.fhce.library.dto.response.library.ReservaResponse;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Reserva;

@Component
public class ReservaMapper {

  public ReservaResponse toResponse(Reserva reserva) {
    ReservaResponse response = new ReservaResponse();

    response.setIdReserva(reserva.getIdReserva());
    response.setEstadoReserva(reserva.getEstadoReserva());
    response.setPrioridad(reserva.getPrioridad());
    response.setFechaReserva(reserva.getFechaReserva());
    response.setFechaVencimientoReserva(reserva.getFechaVencimientoReserva());
    response.setObservaciones(reserva.getObservaciones());

    // Usuario — se asume que persona está cargada vía JOIN FETCH
    if (reserva.getUsuario() != null) {
      response.setUsuarioId(reserva.getUsuario().getId_usuario());
      Persona persona = reserva.getUsuario().getPersona();
      if (persona != null) {
        String nombreCompleto = persona.getNombre()
            + " " + persona.getApellido_pat()
            + (persona.getApellido_mat() != null ? " " + persona.getApellido_mat() : "");
        response.setUsuarioNombreCompleto(nombreCompleto.trim());
      }
    }

    // Libro
    if (reserva.getLibro() != null) {
      response.setLibroId(reserva.getLibro().getIdLibro());
      response.setLibroTitulo(reserva.getLibro().getTitulo());
    }

    // Ejemplar (puede ser null si la reserva está ACTIVA)
    if (reserva.getEjemplar() != null) {
      response.setEjemplarId(reserva.getEjemplar().getIdEjemplar());
      response.setEjemplarCodigo(reserva.getEjemplar().getCodigoEjemplar());
    }

    // Biblioteca
    if (reserva.getBiblioteca() != null) {
      response.setBibliotecaId(reserva.getBiblioteca().getIdBiblioteca());
      response.setBibliotecaNombre(reserva.getBiblioteca().getNombre());
    }

    // Préstamo (solo cuando estado = ATENDIDA)
    if (reserva.getPrestamo() != null) {
      response.setPrestamoId(reserva.getPrestamo().getIdPrestamo());
    }

    return response;
  }
}