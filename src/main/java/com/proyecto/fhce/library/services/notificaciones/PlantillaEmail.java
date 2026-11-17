package com.proyecto.fhce.library.services.notificaciones;

import org.springframework.web.util.HtmlUtils;

/**
 * Plantillas del correo (HTML + texto plano).
 *
 * Todo el contenido variable se ESCAPA antes de insertarlo en el HTML: el
 * asunto y el mensaje pueden incluir títulos de libros o nombres con
 * caracteres como < > &, y no deben interpretarse como marcado.
 * Se arma por concatenación (no con String.format/replace) para que un
 * mensaje que contenga "%s" o "{{...}}" no rompa ni altere la plantilla.
 */
final class PlantillaEmail {

  private static final String PIE =
      "Este es un mensaje automático del Sistema de Gestión Bibliotecaria. "
          + "Por favor no responda a este correo.";

  private PlantillaEmail() {
  }

  static String html(String institucion, String asunto, String mensaje) {
    String cuerpo = HtmlUtils.htmlEscape(mensaje)
        .replace("\r\n", "\n")
        .replace("\n", "<br>");

    return "<!DOCTYPE html>"
        + "<html lang=\"es\"><head><meta charset=\"UTF-8\"></head>"
        + "<body style=\"margin:0;padding:0;background:#f4f5f7;font-family:Arial,Helvetica,sans-serif;\">"
        + "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:#f4f5f7;padding:24px 0;\">"
        + "<tr><td align=\"center\">"
        + "<table role=\"presentation\" width=\"600\" cellpadding=\"0\" cellspacing=\"0\" "
        + "style=\"background:#ffffff;border-radius:8px;overflow:hidden;max-width:600px;width:100%;\">"
        + "<tr><td style=\"background:#1e3a5f;color:#ffffff;padding:20px 28px;font-size:18px;font-weight:bold;\">"
        + HtmlUtils.htmlEscape(institucion)
        + "</td></tr>"
        + "<tr><td style=\"padding:28px;color:#1f2937;font-size:15px;line-height:1.55;\">"
        + "<h2 style=\"margin:0 0 16px 0;font-size:18px;color:#1e3a5f;\">"
        + HtmlUtils.htmlEscape(asunto)
        + "</h2>"
        + "<p style=\"margin:0;\">" + cuerpo + "</p>"
        + "</td></tr>"
        + "<tr><td style=\"padding:16px 28px;background:#f9fafb;color:#6b7280;font-size:12px;\">"
        + PIE
        + "</td></tr>"
        + "</table></td></tr></table></body></html>";
  }

  static String texto(String asunto, String mensaje) {
    return asunto + "\n\n" + mensaje + "\n\n--\n" + PIE;
  }
}
