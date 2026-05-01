package com.proyecto.fhce.library.services.library;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyecto.fhce.library.dto.request.library.LibroLoteRequest;
import com.proyecto.fhce.library.dto.request.library.LibroLoteRequest.EdicionLoteItem;
import com.proyecto.fhce.library.dto.request.library.LibroLoteRequest.EjemplarLoteItem;
import com.proyecto.fhce.library.dto.response.library.*;
import com.proyecto.fhce.library.entities.*;
import com.proyecto.fhce.library.enums.EstadoEjemplar;
import com.proyecto.fhce.library.enums.TipoArchivo;
import com.proyecto.fhce.library.exception.BusinessException;
import com.proyecto.fhce.library.exception.ResourceNotFoundException;
import com.proyecto.fhce.library.repositories.*;
import com.proyecto.fhce.library.security.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class LibroLoteService {

  @Autowired
  private LibroRepository libroRepository;
  @Autowired
  private EdicionRepository edicionRepository;
  @Autowired
  private EjemplarRepository ejemplarRepository;
  @Autowired
  private BibliotecaRepository bibliotecaRepository;
  @Autowired
  private CategoriaLibroRepository categoriaRepository;
  @Autowired
  private AutorRepository autorRepository;
  @Autowired
  private HistorialEstadoEjemplarRepository historialRepository;
  @Autowired
  private StorageService storageService;
  @Autowired
  private SecurityService securityService;
  @Autowired
  private ObjectMapper objectMapper;

  /**
   * Crea un libro completo en una sola transacción:
   * libro → ediciones → ejemplares.
   *
   * Los archivos de portada y PDF se identifican por convención de nombre:
   * portadas → portada_0, portada_1, ... (índice = posición en ediciones[])
   * pdfs → pdf_0, pdf_1, ...
   *
   * Si no llega archivo para un índice se usa imagenPortadaUrl / pdfUrl del JSON.
   * Toda la operación es atómica: si algo falla se hace rollback completo,
   * incluyendo los archivos ya guardados en disco.
   */
  public LibroLoteResponse crearEnLote(
      LibroLoteRequest request,
      Map<String, MultipartFile> archivos) {

    // ── 1. Crear el libro ─────────────────────────────────────────────────
    Libro libro = new Libro();
    libro.setTitulo(request.getTitulo());
    libro.setIdioma(request.getIdioma());

    if (request.getCategoriaId() != null) {
      CategoriaLibro cat = categoriaRepository.findById(request.getCategoriaId())
          .orElseThrow(() -> new ResourceNotFoundException(
              "Categoría no encontrada con id: " + request.getCategoriaId()));
      libro.setCategoria(cat);
    }

    List<Autor> autoresFinales = new ArrayList<>();

    // ── 1. Autores existentes ─────────────────────────────
    if (request.getAutorIds() != null && !request.getAutorIds().isEmpty()) {

      List<Autor> existentes = autorRepository.findAllById(request.getAutorIds());

      if (existentes.size() != request.getAutorIds().size()) {
        throw new ResourceNotFoundException("Uno o más autores no existen");
      }

      autoresFinales.addAll(existentes);
    }

    // ── 2. Autores nuevos ─────────────────────────────────
    if (request.getAutores() != null && !request.getAutores().isEmpty()) {

      for (LibroLoteRequest.AutorItem item : request.getAutores()) {

        // ⚠️ Como NO es obligatorio, lo validamos manualmente
        if (item.getNombre() == null || item.getNombre().isBlank()) {
          continue; // ignora autores vacíos
        }

        // 🔥 evitar duplicados (muy importante)
        Autor autor = autorRepository
            .findByNombreIgnoreCase(item.getNombre())
            .orElseGet(() -> {
              Autor nuevo = new Autor();
              nuevo.setNombre(item.getNombre());
              return autorRepository.save(nuevo);
            });

        autoresFinales.add(autor);
      }
    }

    // ── 3. Asignar autores al libro ───────────────────────
    if (!autoresFinales.isEmpty()) {

      // opcional PRO: sincronizar ambos lados
      for (Autor autor : autoresFinales) {
        autor.getLibros().add(libro);
      }

      libro.setAutores(autoresFinales);
    }

    libro = libroRepository.save(libro);

    // ── 2. Crear ediciones y ejemplares ───────────────────────────────────
    List<String> archivosGuardados = new ArrayList<>();
    List<LibroLoteResponse.EdicionLoteResult> edicionResults = new ArrayList<>();
    int totalEjemplares = 0;

    try {
      List<EdicionLoteItem> edicionItems = request.getEdiciones();

      for (int i = 0; i < edicionItems.size(); i++) {
        EdicionLoteItem item = edicionItems.get(i);

        Edicion edicion = new Edicion();
        edicion.setLibro(libro);
        edicion.setAnoPublicacion(item.getAnoPublicacion());
        edicion.setIsbn(item.getIsbn() != null
            ? item.getIsbn()
            : item.getAnoPublicacion() + "-1");
        edicion.setEdicion(item.getEdicion());
        edicion.setEditorial(item.getEditorial());
        // ── Portada ──────────────────────────────────────────────────
        MultipartFile portadaFile = archivos.get("portada_" + i);
        if (portadaFile != null && !portadaFile.isEmpty()) {
          String url = storageService.guardar(TipoArchivo.PORTADAS, portadaFile);
          archivosGuardados.add(url);
          edicion.setImagenPortada(url);
        } else if (item.getImagenPortadaUrl() != null) {
          edicion.setImagenPortada(item.getImagenPortadaUrl());
        }

        // ── PDF ──────────────────────────────────────────────────────
        MultipartFile pdfFile = archivos.get("pdf_" + i);
        if (pdfFile != null && !pdfFile.isEmpty()) {
          String url = storageService.guardar(TipoArchivo.PDFS, pdfFile);
          archivosGuardados.add(url);
          edicion.setPdfUrl(url);
        } else if (item.getPdfUrl() != null) {
          edicion.setPdfUrl(item.getPdfUrl());
        }

        edicion = edicionRepository.save(edicion);

        // ── Ejemplares de esta edición ────────────────────────────────
        List<LibroLoteResponse.EjemplarLoteResult> ejemplarResults = new ArrayList<>();

        if (item.getEjemplares() != null) {
          for (EjemplarLoteItem ejItem : item.getEjemplares()) {
            Biblioteca biblioteca = bibliotecaRepository
                .findById(ejItem.getBibliotecaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Biblioteca no encontrada con id: " + ejItem.getBibliotecaId()));

            Ejemplar ejemplar = new Ejemplar();
            ejemplar.setEdicion(edicion);
            ejemplar.setBiblioteca(biblioteca);
            ejemplar.setCodigoTopografico(ejItem.getCodigoTopografico());
            ejemplar.setCodigoEjemplar(ejItem.getCodigoEjemplar());
            ejemplar.setObservaciones(ejItem.getObservaciones());
            ejemplar.setEstadoEjemplar(EstadoEjemplar.DISPONIBLE);

            ejemplar = ejemplarRepository.save(ejemplar);

            // Historial de alta
            registrarAlta(ejemplar);

            LibroLoteResponse.EjemplarLoteResult ejResult = new LibroLoteResponse.EjemplarLoteResult();
            ejResult.setIdEjemplar(ejemplar.getIdEjemplar());
            ejResult.setCodigoTopografico(ejemplar.getCodigoTopografico());
            ejResult.setEstadoEjemplar(ejemplar.getEstadoEjemplar().name());
            ejResult.setBibliotecaId(biblioteca.getIdBiblioteca());
            ejResult.setNombreBiblioteca(biblioteca.getNombre());
            ejemplarResults.add(ejResult);

            totalEjemplares++;
          }
        }

        LibroLoteResponse.EdicionLoteResult edResult = new LibroLoteResponse.EdicionLoteResult();
        edResult.setIdEdicion(edicion.getIdEdicion());
        edResult.setAnoPublicacion(edicion.getAnoPublicacion());
        edResult.setImagenPortada(edicion.getImagenPortada());
        edResult.setPdfUrl(edicion.getPdfUrl());
        edResult.setEjemplares(ejemplarResults);
        edicionResults.add(edResult);
      }

    } catch (Exception ex) {
      // Limpiar archivos guardados en disco antes del rollback de BD
      for (String url : archivosGuardados) {
        storageService.eliminar(url, TipoArchivo.PORTADAS);
        storageService.eliminar(url, TipoArchivo.PDFS);
      }
      throw ex;
    }

    // ── 3. Construir respuesta ────────────────────────────────────────────
    return buildResponse(libro, edicionResults, totalEjemplares);
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  private void registrarAlta(Ejemplar ejemplar) {
    HistorialEstadoEjemplar h = new HistorialEstadoEjemplar();
    h.setEjemplar(ejemplar);
    h.setEstadoAnterior(null);
    h.setEstadoNuevo(EstadoEjemplar.DISPONIBLE);
    h.setMotivo("Alta por carga en lote");
    h.setUsuarioCambio(securityService.getCurrentUser());
    historialRepository.save(h);
  }

  private LibroLoteResponse buildResponse(Libro libro,
      List<LibroLoteResponse.EdicionLoteResult> edicionResults,
      int totalEjemplares) {

    LibroLoteResponse r = new LibroLoteResponse();
    r.setIdLibro(libro.getIdLibro());
    r.setTitulo(libro.getTitulo());
    r.setIdioma(libro.getIdioma());
    r.setEdiciones(edicionResults);
    if (libro.getAutores() != null) {
      r.setAutores(mapAutoresToResponse(libro.getAutores()));
    }
    r.setTotalEjemplaresCreados(totalEjemplares);

    if (libro.getCategoria() != null) {
      CategoriaLibroResponse cat = new CategoriaLibroResponse();
      cat.setId_categoria(libro.getCategoria().getId_categoria());
      cat.setNombre_categoria(libro.getCategoria().getNombre_categoria());
      cat.setCodigo_dewey(libro.getCategoria().getCodigo_dewey());
      r.setCategoria(cat);
    }

    if (libro.getAutores() != null) {
      r.setAutores(libro.getAutores().stream().map(a -> {
        AutorResponse ar = new AutorResponse();
        ar.setIdAutor(a.getIdAutor());
        ar.setNombre(a.getNombre());
        return ar;
      }).collect(Collectors.toList()));
    }

    return r;
  }

  private List<AutorResponse> mapAutoresToResponse(List<Autor> autores) {
    if (autores == null) {
      return null;
    }

    return autores.stream().map(a -> {
      AutorResponse ar = new AutorResponse();
      ar.setIdAutor(a.getIdAutor());
      ar.setNombre(a.getNombre());
      return ar;
    }).collect(Collectors.toList());
  }
}