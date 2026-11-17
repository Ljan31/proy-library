package com.proyecto.fhce.library.services.notificaciones;

import java.util.Optional;

import com.proyecto.fhce.library.entities.Notificacion;

/**
 * Obtiene los datos de contacto de un usuario. Separa a los canales de envío
 * del módulo de usuarios: EmailCanalService y SmsCanalService no saben cómo
 * está modelada la persona, solo piden "dame el correo / teléfono de este id".
 */
public interface ContactoUsuarioResolver {

  Optional<String> obtenerEmail(Long idUsuario);

  Optional<String> obtenerTelefono(Long idUsuario);

  Optional<String> obtenerEmail(Notificacion notificacion);

}
