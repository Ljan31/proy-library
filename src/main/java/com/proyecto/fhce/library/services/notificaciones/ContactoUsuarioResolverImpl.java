package com.proyecto.fhce.library.services.notificaciones;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.entities.Notificacion;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.SolicitudCertificado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.exception.DestinatarioInvalidoException;
import com.proyecto.fhce.library.repositories.SolicitudCertificadoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
public class ContactoUsuarioResolverImpl implements ContactoUsuarioResolver {

  private final UserRepository usuarioRepository;
  private final SolicitudCertificadoRepository solicitudRepository;

  public ContactoUsuarioResolverImpl(UserRepository usuarioRepository,
      SolicitudCertificadoRepository solicitudRepository) {
    this.usuarioRepository = usuarioRepository;
    this.solicitudRepository = solicitudRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> obtenerEmail(Long idUsuario) {
    return usuarioRepository.findById(idUsuario)
        .map(Usuario::getPersona)
        .map(Persona::getEmail)
        .map(String::trim)
        .filter(valor -> !valor.isEmpty());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> obtenerTelefono(Long idUsuario) {
    return usuarioRepository.findById(idUsuario)
        .map(Usuario::getPersona)
        .map(Persona::getCelular)
        .map(String::trim)
        .filter(valor -> !valor.isEmpty());
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> obtenerEmail(Notificacion notificacion) {

    // Usuario registrado
    if (notificacion.getIdUsuario() != null) {
      return obtenerEmail(notificacion.getIdUsuario());
    }

    // Solicitante externo de certificado
    if ("SOLICITUD_CERTIFICADO".equals(
        notificacion.getTipoReferencia())) {

      if (notificacion.getIdReferencia() == null) {
        return Optional.empty();
      }

      return solicitudRepository
          .obtenerEmailPorSolicitud(notificacion.getIdReferencia())
          .map(String::trim)
          .filter(email -> !email.isEmpty());
    }

    return Optional.empty();
  }
}
