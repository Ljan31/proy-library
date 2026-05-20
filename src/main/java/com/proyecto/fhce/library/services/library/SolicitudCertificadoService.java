package com.proyecto.fhce.library.services.library;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.request.library.SolicitudCertificadoRequest;
import com.proyecto.fhce.library.dto.response.library.SolicitudCertificadoResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.EstadoSolicitud;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.SolicitudCertificadoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
public class SolicitudCertificadoService {

  private final SolicitudCertificadoRepository solicitudRepository;
  private final BibliotecaRepository bibliotecaRepository;
  private final UserRepository usuarioRepository;

  public SolicitudCertificadoService(
      SolicitudCertificadoRepository solicitudRepository,
      BibliotecaRepository bibliotecaRepository,
      UserRepository usuarioRepository) {
    this.solicitudRepository = solicitudRepository;
    this.bibliotecaRepository = bibliotecaRepository;
    this.usuarioRepository = usuarioRepository;
  }

  // =====================================================
  // CREAR SOLICITUD
  // =====================================================
  @Transactional
  public SolicitudCertificadoResponse crearSolicitud(
      SolicitudCertificadoRequest request,
      Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) {

    Biblioteca biblioteca = bibliotecaRepository.findById(
        request.getBibliotecaId()).orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada"));

    SolicitudCertificado solicitud = new SolicitudCertificado();

    solicitud.setBiblioteca(biblioteca);

    // intentar vincular usuario si existe
    Optional<Usuario> usuarioOpt = usuarioRepository.findByPersona_Ci(request.getCi());

    usuarioOpt.ifPresent(solicitud::setUsuario);

    solicitud.setNombres(request.getNombres());
    solicitud.setApellidos(request.getApellidos());
    solicitud.setCi(request.getCi());
    solicitud.setMatricula(request.getMatricula());

    solicitud.setRazon(request.getRazon());
    solicitud.setDescripcion(request.getDescripcion());

    solicitud.setEstado(EstadoSolicitud.PENDIENTE);
    solicitud.setFechaSolicitud(LocalDateTime.now());

    SolicitudCertificado saved = solicitudRepository.save(solicitud);

    return mapToResponse(saved);
  }

  // =====================================================
  // LISTAR POR BIBLIOTECA
  // =====================================================
  @Transactional(readOnly = true)
  public List<SolicitudCertificadoResponse> listarPorBiblioteca(
      Long bibliotecaId,
      EstadoSolicitud estado,
      Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) {

    List<SolicitudCertificado> lista;

    if (estado != null) {
      lista = solicitudRepository
          .findByBiblioteca_IdBibliotecaAndEstado(
              bibliotecaId,
              estado);
    } else {
      lista = solicitudRepository
          .findByBiblioteca_IdBiblioteca(bibliotecaId);
    }

    return lista.stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  // =====================================================
  // OBTENER POR ID
  // =====================================================
  @Transactional(readOnly = true)
  public SolicitudCertificadoResponse obtenerPorId(
      Long id,
      Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) {

    SolicitudCertificado solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Solicitud no encontrada"));

    return mapToResponse(solicitud);
  }

  // =====================================================
  // APROBAR
  // =====================================================
  @Transactional
  public SolicitudCertificadoResponse aprobarSolicitud(
      Long id,
      String observacion,
      Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) {

    SolicitudCertificado solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Solicitud no encontrada"));

    solicitud.setEstado(EstadoSolicitud.APROBADA);
    solicitud.setFechaRespuesta(LocalDateTime.now());
    solicitud.setObservacionRespuesta(observacion);

    return mapToResponse(
        solicitudRepository.save(solicitud));
  }

  // =====================================================
  // RECHAZAR
  // =====================================================
  @Transactional
  public SolicitudCertificadoResponse rechazarSolicitud(
      Long id,
      String observacion,
      Long solicitanteId,
      Collection<? extends GrantedAuthority> authorities) {

    SolicitudCertificado solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Solicitud no encontrada"));

    solicitud.setEstado(EstadoSolicitud.RECHAZADA);
    solicitud.setFechaRespuesta(LocalDateTime.now());
    solicitud.setObservacionRespuesta(observacion);

    return mapToResponse(
        solicitudRepository.save(solicitud));
  }

  // =====================================================
  // MAPPER
  // =====================================================
  private SolicitudCertificadoResponse mapToResponse(
      SolicitudCertificado solicitud) {

    SolicitudCertificadoResponse response = new SolicitudCertificadoResponse();

    response.setId(solicitud.getId());
    response.setNombres(solicitud.getNombres());
    response.setApellidos(solicitud.getApellidos());
    response.setCi(solicitud.getCi());
    response.setMatricula(solicitud.getMatricula());

    response.setBibliotecaNombre(
        solicitud.getBiblioteca().getNombre());

    response.setRazon(solicitud.getRazon());
    response.setDescripcion(solicitud.getDescripcion());

    response.setEstado(
        solicitud.getEstado().name());

    response.setFechaSolicitud(
        solicitud.getFechaSolicitud());

    response.setFechaRespuesta(
        solicitud.getFechaRespuesta());

    response.setObservacionRespuesta(
        solicitud.getObservacionRespuesta());

    return response;
  }
}