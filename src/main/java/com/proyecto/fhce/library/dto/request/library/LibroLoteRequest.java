package com.proyecto.fhce.library.dto.request.library;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * DTO para la carga en lote de un libro completo:
 * datos del libro + una o más ediciones + ejemplares por edición.
 *
 * Se recibe como multipart/form-data:
 * - parte "datos" → JSON con este DTO (application/json)
 * - parte "portada_0", "portada_1", ... → archivos de imagen por edición
 * (opcional)
 * - parte "pdf_0", "pdf_1", ... → archivos PDF por edición (opcional)
 *
 * El índice del nombre de parte corresponde al índice de la edición en el
 * array.
 */
public class LibroLoteRequest {

  // ── Libro ─────────────────────────────────────────────────────────────────

  @NotBlank(message = "El título del libro es obligatorio")
  @Size(max = 500)
  private String titulo;

  @Size(max = 50)
  private String idioma;

  /** IDs de autores ya existentes en la BD. */
  private List<Long> autorIds;

  private Long categoriaId;
  @Valid
  private List<AutorItem> autores;
  // ── Ediciones ─────────────────────────────────────────────────────────────

  @NotEmpty(message = "Debe incluir al menos una edición")
  @Valid
  private List<EdicionLoteItem> ediciones;

  // ── Getters y setters ─────────────────────────────────────────────────────

  public String getTitulo() {
    return titulo;
  }

  public void setTitulo(String titulo) {
    this.titulo = titulo;
  }

  public String getIdioma() {
    return idioma;
  }

  public void setIdioma(String idioma) {
    this.idioma = idioma;
  }

  public List<Long> getAutorIds() {
    return autorIds;
  }

  public void setAutorIds(List<Long> autorIds) {
    this.autorIds = autorIds;
  }

  public Long getCategoriaId() {
    return categoriaId;
  }

  public void setCategoriaId(Long categoriaId) {
    this.categoriaId = categoriaId;
  }

  public List<EdicionLoteItem> getEdiciones() {
    return ediciones;
  }

  public void setEdiciones(List<EdicionLoteItem> ediciones) {
    this.ediciones = ediciones;
  }

  public List<AutorItem> getAutores() {
    return autores;
  }

  public void setAutores(List<AutorItem> autores) {
    this.autores = autores;
  }
  // ═════════════════════════════════════════════════════════════════════════
  // Clase interna: una edición con sus ejemplares
  // ═════════════════════════════════════════════════════════════════════════

  public static class EdicionLoteItem {

    private Integer anoPublicacion;

    /**
     * URL externa de portada.
     * Si se sube archivo (portada_N), este campo se ignora.
     */
    private String imagenPortadaUrl;

    /**
     * URL externa de PDF.
     * Si se sube archivo (pdf_N), este campo se ignora.
     */
    private String pdfUrl;
    private String isbn;

    private String editorial;

    @Size(max = 50)
    private String edicion;

    @Valid
    private List<EjemplarLoteItem> ejemplares;

    public Integer getAnoPublicacion() {
      return anoPublicacion;
    }

    public void setAnoPublicacion(Integer anoPublicacion) {
      this.anoPublicacion = anoPublicacion;
    }

    public String getImagenPortadaUrl() {
      return imagenPortadaUrl;
    }

    public void setImagenPortadaUrl(String imagenPortadaUrl) {
      this.imagenPortadaUrl = imagenPortadaUrl;
    }

    public String getPdfUrl() {
      return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
      this.pdfUrl = pdfUrl;
    }

    public List<EjemplarLoteItem> getEjemplares() {
      return ejemplares;
    }

    public void setEjemplares(List<EjemplarLoteItem> ejemplares) {
      this.ejemplares = ejemplares;
    }

    public String getIsbn() {
      return isbn;
    }

    public void setIsbn(String isbn) {
      this.isbn = isbn;
    }

    public String getEditorial() {
      return editorial;
    }

    public void setEditorial(String editorial) {
      this.editorial = editorial;
    }

    public String getEdicion() {
      return edicion;
    }

    public void setEdicion(String edicion) {
      this.edicion = edicion;
    }

  }

  // ═════════════════════════════════════════════════════════════════════════
  // Clase interna: un ejemplar físico
  // ═════════════════════════════════════════════════════════════════════════

  public static class EjemplarLoteItem {

    @NotNull(message = "La biblioteca del ejemplar es obligatoria")
    private Long bibliotecaId;

    @Size(max = 100)
    private String codigoTopografico;
    @Size(max = 50)
    private String codigoEjemplar;
    @Size(max = 200)
    private String observaciones;

    public Long getBibliotecaId() {
      return bibliotecaId;
    }

    public void setBibliotecaId(Long bibliotecaId) {
      this.bibliotecaId = bibliotecaId;
    }

    public String getCodigoTopografico() {
      return codigoTopografico;
    }

    public void setCodigoTopografico(String codigoTopografico) {
      this.codigoTopografico = codigoTopografico;
    }

    public String getCodigoEjemplar() {
      return codigoEjemplar;
    }

    public void setCodigoEjemplar(String codigoEjemplar) {
      this.codigoEjemplar = codigoEjemplar;
    }

    public String getObservaciones() {
      return observaciones;
    }

    public void setObservaciones(String observaciones) {
      this.observaciones = observaciones;
    }

  }

  public static class AutorItem {

    @NotBlank(message = "El nombre del autor es obligatorio")
    @Size(max = 200)
    private String nombre;

    public String getNombre() {
      return nombre;
    }

    public void setNombre(String nombre) {
      this.nombre = nombre;
    }
  }
}