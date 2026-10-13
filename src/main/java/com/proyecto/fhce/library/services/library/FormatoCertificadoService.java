package com.proyecto.fhce.library.services.library;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
// import java.time.LocalDate;
// import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
// import java.util.Optional;
// import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.core.GrantedAuthority;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
// import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.proyecto.fhce.library.dto.response.library.FormatoCertificadoResponse;
import com.proyecto.fhce.library.entities.Biblioteca;
import com.proyecto.fhce.library.entities.FormatoCertificado;
import com.proyecto.fhce.library.entities.Persona;
import com.proyecto.fhce.library.entities.Usuario;
import com.proyecto.fhce.library.enums.TipoArchivo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.BibliotecaRepository;
import com.proyecto.fhce.library.repositories.FormatoCertificadoRepository;
import com.proyecto.fhce.library.repositories.UserRepository;
import com.proyecto.fhce.library.services.DocxToPdfConverter;

@Service
@Transactional
public class FormatoCertificadoService {

  private static final Logger log = LoggerFactory.getLogger(FormatoCertificadoService.class);
  private static final List<String> EXTENSIONES_PERMITIDAS = List.of(".docx");
  private static final long TAMANIO_MAXIMO = 10 * 1024 * 1024; // 10 MB

  @Value("${app.uploads.bibliotecas:/uploads/bibliotecas}")
  private String rutaBase;

  @Autowired
  private FormatoCertificadoRepository formatoRepo;
  @Autowired
  private BibliotecaRepository bibliotecaRepo;
  @Autowired
  private UserRepository usuarioRepo;
  @Autowired
  private StorageService storageService;
  @Autowired
  private DocxToPdfConverter pdfConverter;
  // ── Consulta ─────────────────────────────────────────────────────────────

  @Transactional(readOnly = true)
  public FormatoCertificadoResponse obtenerActivo(Long bibliotecaId) {
    return formatoRepo.findByBiblioteca_IdBibliotecaAndActivoTrue(bibliotecaId)
        .map(this::mapToResponse)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No hay plantilla activa para la biblioteca: " + bibliotecaId));
  }

  @Transactional(readOnly = true)
  public List<FormatoCertificadoResponse> historial(Long bibliotecaId) {
    return formatoRepo.findByBiblioteca_IdBibliotecaOrderByVersionDesc(bibliotecaId)
        .stream().map(this::mapToResponse).toList();
  }

  // ── Subida de plantilla ───────────────────────────────────────────────────

  public FormatoCertificadoResponse subirPlantilla(
      Long bibliotecaId,
      MultipartFile archivo,
      Long usuarioId,
      Collection<? extends GrantedAuthority> authorities) throws IOException {

    validarArchivo(archivo);

    Biblioteca biblioteca = bibliotecaRepo.findById(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException("Biblioteca no encontrada: " + bibliotecaId));

    // BIBLIOTECARIO solo puede subir para su propia biblioteca
    boolean esAdmin = tieneRol(authorities, "ROLE_ADMIN");
    if (!esAdmin) {
      // boolean esEncargado =
      // biblioteca.getEncargado().getIdUsuario().equals(usuarioId);
      boolean esEncargado = biblioteca.getEncargados().stream().anyMatch(
          enc -> enc.getUsuario().getId_usuario().equals(usuarioId));
      if (!esEncargado)
        throw new BusinessException("No puede modificar plantillas de otra biblioteca");
    }

    // Calcular siguiente versión
    int siguienteVersion = formatoRepo
        .findByBiblioteca_IdBibliotecaOrderByVersionDesc(bibliotecaId)
        .stream()
        .findFirst()
        .map(f -> f.getVersion() + 1)
        .orElse(1);

    // StorageService guarda el archivo y devuelve la URL relativa
    // La validación de .docx la hace StorageService internamente
    String urlArchivo = storageService.guardar(TipoArchivo.PLANTILLAS_CERTIFICADO, archivo);

    // Desactivar versión anterior y guardar el archivo
    formatoRepo.desactivarTodas(bibliotecaId);

    Usuario subidoPor = usuarioRepo.findById(usuarioId)
        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

    FormatoCertificado formato = new FormatoCertificado();
    formato.setBiblioteca(biblioteca);
    formato.setNombreArchivo(archivo.getOriginalFilename());
    formato.setRutaArchivo(urlArchivo);
    formato.setVersion(siguienteVersion);
    formato.setActivo(true);
    formato.setSubidoPor(subidoPor);
    formato.setFechaActualizacion(LocalDateTime.now());

    return mapToResponse(formatoRepo.save(formato));
  }

  // ── Carga en memoria (para generación) ───────────────────────────────────
  @Transactional(readOnly = true)
  public byte[] cargarPlantillaEnMemoria(Long bibliotecaId) throws Exception, IOException {
    FormatoCertificado formato = formatoRepo
        .findByBiblioteca_IdBibliotecaAndActivoTrue(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "No hay plantilla activa para la biblioteca: " + bibliotecaId));

    // Delegar lectura en StorageService — él conoce la ruta base real
    byte[] docx = storageService.cargarEnMemoria(
        formato.getRutaArchivo(),
        TipoArchivo.PLANTILLAS_CERTIFICADO);
    byte[] pdf = pdfConverter.convertir(docx);
    return pdf;
  }

  // ── Validaciones ─────────────────────────────────────────────────────────

  public Resource obtenerArchivo(Long id) {

    FormatoCertificado formato = formatoRepo
        .findById(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Formato de certificado no encontrado: " + id));

    if (!Boolean.TRUE.equals(formato.getActivo())) {
      throw new ResourceNotFoundException(
          "El formato de certificado no está activo: " + id);
    }

    return storageService.cargarComoResource(
        formato.getRutaArchivo(),
        TipoArchivo.PLANTILLAS_CERTIFICADO);
  }

  /**
   * Sube firma o sello para una biblioteca.
   * Reemplaza la imagen anterior (la plantilla .docx NO se toca).
   * tipo: "firma" | "sello"
   */
  public FormatoCertificadoResponse subirImagen(
      Long bibliotecaId,
      String tipo,
      MultipartFile archivo,
      Long usuarioId,
      Collection<? extends GrantedAuthority> authorities) {

    Biblioteca biblioteca = bibliotecaRepo.findById(bibliotecaId)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Biblioteca no encontrada: " + bibliotecaId));

    // Solo el encargado o admin
    boolean esAdmin = tieneRol(authorities, "ROLE_ADMIN");
    boolean esEncargado = biblioteca.getEncargados().stream().anyMatch(
        enc -> enc.getUsuario().getId_usuario().equals(usuarioId));
    if (!esAdmin && !esEncargado)
      throw new BusinessException("No autorizado para esta biblioteca");

    // Buscar o crear el registro activo (puede no tener plantilla .docx aún)
    FormatoCertificado formato = formatoRepo
        .findByBiblioteca_IdBibliotecaAndActivoTrue(bibliotecaId)
        .orElseGet(() -> {
          // Crear registro mínimo si no existe
          FormatoCertificado nuevo = new FormatoCertificado();
          nuevo.setBiblioteca(biblioteca);
          nuevo.setNombreArchivo("sin-plantilla");
          nuevo.setRutaArchivo("");
          nuevo.setVersion(1);
          nuevo.setActivo(true);
          return nuevo;
        });

    // Determinar tipo de archivo y actualizar campo correspondiente
    TipoArchivo tipoArchivo = resolverTipoImagen(tipo);
    String urlNueva = storageService.guardar(tipoArchivo, archivo);

    // Eliminar imagen anterior del disco si existe
    String urlAnterior = tipo.equals("firma") ? formato.getFirmaUrl() : formato.getSelloUrl();
    if (urlAnterior != null && !urlAnterior.isBlank()) {
      storageService.eliminar(urlAnterior, tipoArchivo);
    }

    if (tipo.equals("firma")) {
      formato.setFirmaUrl(urlNueva);
    } else {
      formato.setSelloUrl(urlNueva);
    }

    formato.setFechaActualizacion(LocalDateTime.now());
    return mapToResponse(formatoRepo.save(formato));
  }

  private TipoArchivo resolverTipoImagen(String tipo) {
    return switch (tipo.toLowerCase()) {
      case "firma" -> TipoArchivo.FIRMAS_CERTIFICADO;
      case "sello" -> TipoArchivo.SELLOS_CERTIFICADO;
      default -> throw new BusinessException("Tipo de imagen inválido: " + tipo);
    };
  }

  public FormatoCertificado obtenerPorId(Long id) {

    return formatoRepo
        .buscarPorIdConBiblioteca(id)
        .orElseThrow(() -> new ResourceNotFoundException(
            "Formato de certificado no encontrado: " + id));
  }

  private void validarArchivo(MultipartFile archivo) {
    if (archivo == null || archivo.isEmpty())
      throw new BusinessException("El archivo no puede estar vacío");

    if (archivo.getSize() > TAMANIO_MAXIMO)
      throw new BusinessException("El archivo supera el tamaño máximo permitido de 10 MB");

    String nombre = StringUtils.cleanPath(archivo.getOriginalFilename() != null
        ? archivo.getOriginalFilename()
        : "");

    boolean extensionValida = EXTENSIONES_PERMITIDAS.stream()
        .anyMatch(ext -> nombre.toLowerCase().endsWith(ext));

    if (!extensionValida)
      throw new BusinessException("Solo se permiten archivos .docx");
  }

  private boolean tieneRol(Collection<? extends GrantedAuthority> authorities, String rol) {
    return authorities.stream().anyMatch(a -> a.getAuthority().equals(rol));
  }

  private FormatoCertificadoResponse mapToResponse(FormatoCertificado f) {
    FormatoCertificadoResponse r = new FormatoCertificadoResponse();
    r.setIdFormato(f.getIdFormato());
    r.setBibliotecaId(f.getBiblioteca().getIdBiblioteca());
    r.setNombreBiblioteca(f.getBiblioteca().getNombre());
    r.setUrl(f.getRutaArchivo()); // ← Vue espera "url"
    r.setNombreArchivo(f.getNombreArchivo());
    r.setFirmaUrl(f.getFirmaUrl());
    r.setSelloUrl(f.getSelloUrl());
    r.setVersion(f.getVersion());
    r.setActivo(f.getActivo());
    r.setFechaCreacion(f.getFechaCreacion());
    r.setFechaActualizacion(f.getFechaActualizacion());
    if (f.getSubidoPor() != null && f.getSubidoPor().getPersona() != null) {
      Persona p = f.getSubidoPor().getPersona();
      r.setSubidoPor(p.getNombre() + " " + p.getApellido_pat());
    }
    return r;
  }
}