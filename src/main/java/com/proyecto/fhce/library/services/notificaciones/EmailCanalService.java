// package com.proyecto.fhce.library.services.notificaciones;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.mail.SimpleMailMessage;
// import org.springframework.mail.MailException;
// import org.springframework.mail.javamail.JavaMailSender;
// import org.springframework.stereotype.Service;

// /**
// * Adaptador de envío por EMAIL usando Spring Mail.
// */
// @Service
// public class EmailCanalService implements CanalEnvioService {

// private static final Logger log =
// LoggerFactory.getLogger(EmailCanalService.class);

// private final JavaMailSender mailSender;

// private static final String REMITENTE = "noreply@sigeb.edu.bo";

// // Constructor manual (reemplazo de @RequiredArgsConstructor)
// public EmailCanalService(JavaMailSender mailSender) {
// this.mailSender = mailSender;
// }

// @Override
// public CanalNotificacion canal() {
// return CanalNotificacion.EMAIL;
// }

// @Override
// public void enviar(Notificacion notificacion) {

// String emailDestinatario = resolverEmail(notificacion.getIdUsuario());

// if (emailDestinatario == null || emailDestinatario.isBlank()) {
// throw new IllegalStateException(
// "El usuario " + notificacion.getIdUsuario() + " no tiene email registrado");
// }

// try {
// SimpleMailMessage mensaje = new SimpleMailMessage();
// mensaje.setFrom(REMITENTE);
// mensaje.setTo(emailDestinatario);
// mensaje.setSubject(notificacion.getAsunto());
// mensaje.setText(notificacion.getMensaje());

// mailSender.send(mensaje);

// log.info("Email enviado a {} para notificación {}",
// emailDestinatario,
// notificacion.getIdNotificacion());

// } catch (MailException e) {
// log.error("Fallo al enviar email para notificación {}: {}",
// notificacion.getIdNotificacion(),
// e.getMessage());

// throw new RuntimeException("Error al enviar email: " + e.getMessage(), e);
// }
// }

// private String resolverEmail(Long idUsuario) {
// // TODO: implementar con UserRepository
// return null;
// }
// }