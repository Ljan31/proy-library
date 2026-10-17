package com.proyecto.fhce.library.services.library;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.fhce.library.dto.response.library.UserCertificadoResponse;
import com.proyecto.fhce.library.entities.FormatoCertificado;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.TipoArchivo;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.FormatoCertificadoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;

@Service
@Transactional
public class CertificadoDocxService {

  private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy",
      new Locale("es", "BO"));
  private static final Logger log = LoggerFactory.getLogger(CertificadoDocxService.class);

  private final FormatoCertificadoService formatoService;
  private final FormatoCertificadoRepository formatoRepo;
  private final DocxPlaceholderService placeholderService;
  private final UserRepository usuarioRepo;
  private final StorageService storageService;
  private final QrGeneratorService qrGeneratorService;

  public CertificadoDocxService(FormatoCertificadoService formatoService, FormatoCertificadoRepository formatoRepo,
      DocxPlaceholderService placeholderService, UserRepository usuarioRepo, StorageService storageService,
      QrGeneratorService qrGeneratorService) {
    this.formatoService = formatoService;
    this.formatoRepo = formatoRepo;
    this.placeholderService = placeholderService;
    this.usuarioRepo = usuarioRepo;
    this.storageService = storageService;
    this.qrGeneratorService = qrGeneratorService;
  }

  public byte[] generar(UserCertificadoResponse userData) throws Exception {
    // 2. Cargar plantilla en memoria (sin tocar el archivo original)
    byte[] plantilla = formatoService.cargarPlantillaEnMemoriaDOCX(userData.getBibliotecaId());
    // 3. Construir variables para reemplazar
    Map<String, String> variables = construirVariables(userData);

    // Cargar imágenes en memoria si existen
    Map<String, byte[]> imagenes = cargarImagenes(userData.getBibliotecaId(), userData.getCodigoVerificacion());

    // 4. Reemplazar placeholders y retornar el documento generado
    return placeholderService.reemplazar(plantilla, variables, imagenes);
  }

  private Map<String, String> construirVariables(UserCertificadoResponse userData) {
    Map<String, String> vars = new LinkedHashMap<>();
    vars.put("nombre", userData.getNombreCompleto());
    vars.put("ci", userData.getCi());
    vars.put("biblioteca", userData.getNombreBiblioteca());
    vars.put("ciudad", "La Paz");
    vars.put("fecha", userData.getFechaEmision().format(FORMATO_FECHA));
    vars.put("responsable", "Franz Canaza Aduviri");

    // Valores por defecto para personas no registradas
    vars.put("codigo", "N/D");
    vars.put("email", "");
    vars.put("celular", "");

    // Consultar Usuario solamente si existe un usuarioId
    if (userData.getUsuarioId() != null) {

      Usuario usuario = usuarioRepo.findById(userData.getUsuarioId())
          .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

      // Obtener matrícula
      if (usuario.getCarreras() != null
          && !usuario.getCarreras().isEmpty()) {

        String matricula = usuario.getCarreras()
            .iterator()
            .next()
            .getMatricula();

        if (matricula != null && !matricula.isBlank()) {
          vars.put("codigo", matricula);
        }
      }

      // Obtener email y celular
      if (usuario.getPersona() != null) {

        String email = usuario.getPersona().getEmail();
        String celular = usuario.getPersona().getCelular();

        vars.put("email", email != null ? email : "");
        vars.put("celular", celular != null ? celular : "");
      }
    }
    return vars;
  }

  private Map<String, byte[]> cargarImagenes(Long bibliotecaId, String codigoVerificacion) {
    Map<String, byte[]> imagenes = new HashMap<>();

    try {
      FormatoCertificado formato = formatoRepo
          .findByBiblioteca_IdBibliotecaAndActivoTrue(bibliotecaId)
          .orElse(null);

      if (formato != null) {
        if (formato.getFirmaUrl() != null && !formato.getFirmaUrl().isBlank()) {
          imagenes.put("firma", storageService.cargarEnMemoria(
              formato.getFirmaUrl(), TipoArchivo.FIRMAS_CERTIFICADO));
        }
        if (formato.getSelloUrl() != null && !formato.getSelloUrl().isBlank()) {
          imagenes.put("sello", storageService.cargarEnMemoria(
              formato.getSelloUrl(), TipoArchivo.SELLOS_CERTIFICADO));
        }
      }
      // QR siempre se genera en memoria — no se guarda en disco
      if (codigoVerificacion != null && !codigoVerificacion.isBlank()) {
        imagenes.put("qr", qrGeneratorService.generarQr(codigoVerificacion));
      }

    } catch (Exception e) {
      // Las imágenes son opcionales — si fallan, se genera sin ellas
      log.warn("No se pudieron cargar imágenes para biblioteca {}: {}", bibliotecaId, e.getMessage());
    }

    return imagenes;
  }

}