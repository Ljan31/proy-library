package com.proyecto.fhce.library.services.notificaciones;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.proyecto.fhce.library.entities.Notificacion;
import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;
import com.proyecto.fhce.library.exception.DestinatarioInvalidoException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Canal EMAIL por SMTP (Gmail, Brevo u otro). Ver
 * docs/GUIA_ENVIO_CORREO_SMS.md.
 *
 * - Si no hay SMTP configurado la aplicación arranca igual; el canal queda
 * "no disponible" y las notificaciones EMAIL se marcan FALLIDO con un
 * mensaje claro.
 * - Envía HTML con alternativa en texto plano.
 */
@Service
public class EmailCanalService implements CanalEnvioService {

  private static final Logger log = LoggerFactory.getLogger(EmailCanalService.class);
  private static final Pattern EMAIL_VALIDO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  // ObjectProvider: Spring solo crea JavaMailSender si hay spring.mail.host.
  // Inyectarlo directo haría fallar el arranque en entornos sin correo.
  private final ObjectProvider<JavaMailSender> mailSenderProvider;
  private final ContactoUsuarioResolver contactoResolver;
  private final boolean habilitado;
  private final String remitente;
  private final String nombreRemitente;
  private final int limiteDiario;

  public EmailCanalService(
      ObjectProvider<JavaMailSender> mailSenderProvider,
      ContactoUsuarioResolver contactoResolver,
      @Value("${sigeb.notificaciones.email.habilitado:false}") boolean habilitado,
      @Value("${sigeb.notificaciones.email.remitente:${spring.mail.username:}}") String remitente,
      @Value("${sigeb.notificaciones.email.nombre-remitente:SIGEB - Biblioteca}") String nombreRemitente,
      @Value("${sigeb.notificaciones.email.limite-diario:400}") int limiteDiario) {
    this.mailSenderProvider = mailSenderProvider;
    this.contactoResolver = contactoResolver;
    this.habilitado = habilitado;
    this.remitente = remitente == null ? "" : remitente.trim();
    this.nombreRemitente = nombreRemitente;
    this.limiteDiario = limiteDiario;
  }

  @Override
  public CanalNotificacion canal() {
    return CanalNotificacion.EMAIL;
  }

  @Override
  public boolean estaDisponible() {
    boolean smtpDisponible = mailSenderProvider.getIfAvailable() != null;

    log.info(
        "Estado EMAIL: habilitado={}, remitenteConfigurado={}, smtpDisponible={}",
        habilitado,
        !remitente.isEmpty(),
        smtpDisponible);
    return habilitado
        && !remitente.isEmpty()
        && mailSenderProvider.getIfAvailable() != null;
  }

  @Override
  public int limiteDiario() {
    return limiteDiario;
  }

  @Override
  public void enviar(Notificacion notificacion) {
    JavaMailSender sender = mailSenderProvider.getIfAvailable();
    if (sender == null) {
      throw new IllegalStateException("SMTP no configurado (falta spring.mail.host)");
    }

    // Obtener correo según el tipo de destinatario
    String destino = contactoResolver
        .obtenerEmail(notificacion)
        .filter(correo -> EMAIL_VALIDO.matcher(correo).matches())
        .orElseThrow(() -> new DestinatarioInvalidoException(
            "No se encontró un correo válido para la notificación "
                + notificacion.getIdNotificacion()));

    try {
      MimeMessage mime = sender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
      helper.setFrom(remitente, nombreRemitente);
      helper.setTo(destino);
      helper.setSubject(unaLinea(notificacion.getAsunto()));
      helper.setText(
          PlantillaEmail.texto(notificacion.getAsunto(), notificacion.getMensaje()),
          PlantillaEmail.html(nombreRemitente, notificacion.getAsunto(), notificacion.getMensaje()));

      sender.send(mime);
      log.info("Email enviado: notificacion={} destino={}",
          notificacion.getIdNotificacion(), enmascarar(destino));

    } catch (MailException | MessagingException | UnsupportedEncodingException e) {
      throw new IllegalStateException("Error al enviar email: " + e.getMessage(), e);
    }
  }

  /** Quita saltos de línea del asunto (evita inyección de cabeceras). */
  private static String unaLinea(String texto) {
    return texto == null ? "" : texto.replaceAll("[\\r\\n]+", " ").trim();
  }

  /**
   * juan.perez@dominio.com → j***@dominio.com (para no loguear correos
   * completos).
   */
  private static String enmascarar(String correo) {
    int arroba = correo.indexOf('@');
    return arroba <= 1 ? "***" : correo.charAt(0) + "***" + correo.substring(arroba);
  }
}
