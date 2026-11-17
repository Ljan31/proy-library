package com.proyecto.fhce.library.services.notificaciones;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.proyecto.fhce.library.entities.Notificacion;
import com.proyecto.fhce.library.enums.notificaciones.CanalNotificacion;
import com.proyecto.fhce.library.exception.DestinatarioInvalidoException;

/**
 * Canal SMS (OPCIONAL) usando "SMS Gateway for Android" (código abierto,
 * Apache-2.0): el celular de la biblioteca, con su propio chip, hace de
 * pasarela SMS y este servicio le envía las órdenes por HTTP.
 *
 * "Gratis" significa sin pagar a un proveedor: cada SMS lo descuenta el plan
 * del chip. Por eso hay un tope diario y el texto se recorta a 160 caracteres.
 *
 * ENVIADO = el gateway aceptó el mensaje. El celular lo despacha después; no
 * hay confirmación de entrega en este flujo.
 *
 * Apagado por defecto: sigeb.notificaciones.sms.habilitado=false.
 */
@Service
public class SmsCanalService implements CanalEnvioService {

  private static final Logger log = LoggerFactory.getLogger(SmsCanalService.class);
  private static final int MAX_CARACTERES = 160;
  private static final Pattern INTERNACIONAL = Pattern.compile("^\\+\\d{8,15}$");
  private static final Pattern LOCAL_BOLIVIA = Pattern.compile("^\\d{8}$");

  private final ContactoUsuarioResolver contactoResolver;
  private final boolean habilitado;
  private final String url;
  private final String usuario;
  private final String password;
  private final String codigoPais;
  private final int limiteDiario;
  private final RestClient restClient;

  public SmsCanalService(
      ContactoUsuarioResolver contactoResolver,
      @Value("${sigeb.notificaciones.sms.habilitado:false}") boolean habilitado,
      @Value("${sigeb.notificaciones.sms.url:}") String url,
      @Value("${sigeb.notificaciones.sms.usuario:}") String usuario,
      @Value("${sigeb.notificaciones.sms.password:}") String password,
      @Value("${sigeb.notificaciones.sms.codigo-pais:+591}") String codigoPais,
      @Value("${sigeb.notificaciones.sms.limite-diario:100}") int limiteDiario) {
    this.contactoResolver = contactoResolver;
    this.habilitado = habilitado;
    this.url = url == null ? "" : url.trim();
    this.usuario = usuario == null ? "" : usuario.trim();
    this.password = password == null ? "" : password;
    this.codigoPais = codigoPais;
    this.limiteDiario = limiteDiario;

    // Timeouts: si el celular está apagado o fuera de la red, no bloquear el scheduler.
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(10_000);
    factory.setReadTimeout(15_000);
    this.restClient = RestClient.builder().requestFactory(factory).build();
  }

  @Override
  public CanalNotificacion canal() {
    return CanalNotificacion.SMS;
  }

  @Override
  public boolean estaDisponible() {
    return habilitado && !url.isEmpty() && !usuario.isEmpty();
  }

  @Override
  public int limiteDiario() {
    return limiteDiario;
  }

  @Override
  public void enviar(Notificacion notificacion) {
    String crudo = contactoResolver.obtenerTelefono(notificacion.getIdUsuario())
        .orElseThrow(() -> new DestinatarioInvalidoException(
            "El usuario " + notificacion.getIdUsuario() + " no tiene teléfono registrado"));
    String destino = normalizarTelefono(crudo, notificacion.getIdUsuario());
    String texto = armarTexto(notificacion);

    // Formato del gateway: {"textMessage":{"text":"..."},"phoneNumbers":["+591..."]}
    Map<String, Object> cuerpo = Map.of(
        "textMessage", Map.of("text", texto),
        "phoneNumbers", List.of(destino));

    try {
      restClient.post()
          .uri(url)
          .headers(h -> h.setBasicAuth(usuario, password))
          .contentType(MediaType.APPLICATION_JSON)
          .body(cuerpo)
          .retrieve()
          .toBodilessEntity();
      log.info("SMS aceptado por el gateway: notificacion={} destino={}",
          notificacion.getIdNotificacion(), enmascarar(destino));
    } catch (RestClientException e) {
      throw new IllegalStateException("Error al enviar SMS: " + e.getMessage(), e);
    }
  }

  /**
   * Deja el número en formato internacional (+591XXXXXXXX).
   * Acepta: +591 7xxxxxxx, 00591 7xxxxxxx, o los 8 dígitos locales de Bolivia.
   */
  private String normalizarTelefono(String crudo, Long idUsuario) {
    String limpio = crudo.replaceAll("[\\s\\-().]", "");
    if (limpio.startsWith("00")) {
      limpio = "+" + limpio.substring(2);
    }
    if (LOCAL_BOLIVIA.matcher(limpio).matches()) {
      limpio = codigoPais + limpio;
    }
    if (!INTERNACIONAL.matcher(limpio).matches()) {
      throw new DestinatarioInvalidoException(
          "El usuario " + idUsuario + " tiene un teléfono con formato inválido");
    }
    return limpio;
  }

  /** Sin tildes y máx. 160 caracteres: así el SMS cuenta como 1 solo mensaje. */
  private static String armarTexto(Notificacion notificacion) {
    String base = notificacion.getAsunto() + ": " + notificacion.getMensaje();
    String sinTildes = Normalizer.normalize(base, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .replaceAll("\\s+", " ")
        .trim();
    return sinTildes.length() > MAX_CARACTERES
        ? sinTildes.substring(0, MAX_CARACTERES - 3) + "..."
        : sinTildes;
  }

  private static String enmascarar(String telefono) {
    return telefono.length() <= 3 ? "***" : "***" + telefono.substring(telefono.length() - 3);
  }
}
