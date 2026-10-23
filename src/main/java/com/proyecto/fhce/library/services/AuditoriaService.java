package com.proyecto.fhce.library.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.fhce.library.dto.response.AuditoriaResponse;
import com.proyecto.fhce.library.dto.response.users.UsuarioSimpleResponse;
import com.proyecto.fhce.library.entities.Auditoria;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.repositories.AuditoriaRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class AuditoriaService {

  private final AuditoriaRepository auditoriaRepository;
  private final UserRepository usuarioRepository;
  private final HttpServletRequest request;
  private final ObjectMapper mapper;

  public AuditoriaService(AuditoriaRepository auditoriaRepository, UserRepository usuarioRepository,
      HttpServletRequest request, ObjectMapper mapper) {
    this.auditoriaRepository = auditoriaRepository;
    this.usuarioRepository = usuarioRepository;
    this.request = request;
    this.mapper = mapper;
  }

  public void registrar(String accion, String tabla, Long idRegistro,
      Object valoresAnteriores, Object valoresNuevos) {
    try {
      Auditoria auditoria = new Auditoria();
      auditoria.setAccion(accion);
      auditoria.setTablaAfectada(tabla);
      auditoria.setId_registro(idRegistro);

      // Convertir objetos a JSON
      // ObjectMapper mapper = new ObjectMapper();
      if (valoresAnteriores != null) {
        auditoria.setValoresAnteriores(mapper.writeValueAsString(valoresAnteriores));
      }
      if (valoresNuevos != null) {
        auditoria.setValoresNuevos(mapper.writeValueAsString(valoresNuevos));
      }

      // Obtener IP del request
      auditoria.setIpAddress(getClientIP());

      // Obtener usuario actual (desde SecurityContext)
      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.getPrincipal() instanceof UserDetails) {
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        Usuario usuario = usuarioRepository.findByUsername(userDetails.getUsername()).orElse(null);
        auditoria.setUsuario(usuario);
      }

      auditoriaRepository.save(auditoria);
    } catch (Exception e) {
      // Log pero no fallar la transacción principal
      System.err.println("Error en auditoría: " + e.getMessage());
    }

  }

  public List<AuditoriaResponse> findAll() {
    return auditoriaRepository.findAll()
        .stream()
        .sorted((a, b) -> b.getFechaHora().compareTo(a.getFechaHora()))
        .map(this::convertirResponse)
        .toList();
  }

  public List<AuditoriaResponse> findByUsuario(Long usuarioId) {
    return auditoriaRepository
        .findByUsuarioOrderByFechaDesc(
            usuarioId,
            org.springframework.data.domain.Pageable.unpaged())
        .getContent()
        .stream()
        .map(this::convertirResponse)
        .toList();
  }

  public List<AuditoriaResponse> findByTabla(String tabla) {
    return auditoriaRepository.findByTablaAfectada(tabla)
        .stream()
        .map(this::convertirResponse)
        .toList();
  }

  public List<AuditoriaResponse> findByRegistro(
      String tabla, Long idRegistro) {
    return auditoriaRepository.findByTablaAndRegistro(tabla, idRegistro)
        .stream()
        .map(this::convertirResponse)
        .toList();
  }

  public List<AuditoriaResponse> findByAccion(String accion) {
    return auditoriaRepository.findByAccion(accion)
        .stream()
        .map(this::convertirResponse)
        .toList();
  }

  public List<AuditoriaResponse> findByFechas(
      LocalDateTime inicio, LocalDateTime fin) {
    return auditoriaRepository.findByFechaHoraBetween(inicio, fin)
        .stream()
        .map(this::convertirResponse)
        .toList();
  }

  private AuditoriaResponse convertirResponse(Auditoria auditoria) {

    AuditoriaResponse response = new AuditoriaResponse();

    response.setId_auditoria(auditoria.getId_auditoria());
    response.setAccion(auditoria.getAccion());
    response.setTablaAfectada(auditoria.getTablaAfectada());
    response.setId_registro(auditoria.getId_registro());
    response.setValoresAnteriores(auditoria.getValoresAnteriores());
    response.setValoresNuevos(auditoria.getValoresNuevos());
    response.setFechaHora(auditoria.getFechaHora());
    response.setIpAddress(auditoria.getIpAddress());

    if (auditoria.getUsuario() != null) {
      Usuario usuario = auditoria.getUsuario();

      UsuarioSimpleResponse usuarioResponse = new UsuarioSimpleResponse();
      usuarioResponse.setId_usuario(usuario.getId_usuario());
      usuarioResponse.setUsername(usuario.getUsername());
      usuarioResponse.setNombreCompleto(
          usuario.getPersona().getNombreCompleto());
      usuarioResponse.setEmail(usuario.getPersona().getEmail());
      usuarioResponse.setCi(usuario.getPersona().getCi());

      response.setUsuario(usuarioResponse);
    }
    return response;
  }

  private String getClientIP() {
    String xfHeader = request.getHeader("X-Forwarded-For");
    if (xfHeader == null) {
      return request.getRemoteAddr();
    }
    return xfHeader.split(",")[0];
  }
}
