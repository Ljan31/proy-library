package com.proyecto.fhce.library.dto.response.library;

import java.util.List;

import jakarta.validation.constraints.Size;

/**
 * Resultado de la carga en lote.
 * Devuelve exactamente lo que se creó para que el frontend
 * pueda actualizar su estado sin hacer llamadas adicionales.
 */
public class LibroLoteResponse {

  private Long idLibro;
  private String titulo;
  private String idioma;
  private String autorTexto;
  private CategoriaLibroResponse categoria;
  private List<AutorResponse> autores;

  private List<EdicionLoteResult> ediciones;

  /** Total de ejemplares creados en este lote. */
  private int totalEjemplaresCreados;

  public Long getIdLibro() {
    return idLibro;
  }

  public void setIdLibro(Long idLibro) {
    this.idLibro = idLibro;
  }

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

  public String getAutorTexto() {
    return autorTexto;
  }

  public void setAutorTexto(String autorTexto) {
    this.autorTexto = autorTexto;
  }

  public CategoriaLibroResponse getCategoria() {
    return categoria;
  }

  public void setCategoria(CategoriaLibroResponse categoria) {
    this.categoria = categoria;
  }

  public List<AutorResponse> getAutores() {
    return autores;
  }

  public void setAutores(List<AutorResponse> autores) {
    this.autores = autores;
  }

  public List<EdicionLoteResult> getEdiciones() {
    return ediciones;
  }

  public void setEdiciones(List<EdicionLoteResult> ediciones) {
    this.ediciones = ediciones;
  }

  public int getTotalEjemplaresCreados() {
    return totalEjemplaresCreados;
  }

  public void setTotalEjemplaresCreados(int totalEjemplaresCreados) {
    this.totalEjemplaresCreados = totalEjemplaresCreados;
  }

  // ── Resultado por edición ─────────────────────────────────────────────────

  public static class EdicionLoteResult {
    private Long idEdicion;
    private Integer anoPublicacion;
    private String isbn;
    private String editorial;
    private String edicion;
    private String imagenPortada;
    private String pdfUrl;
    private List<EjemplarLoteResult> ejemplares;

    public Long getIdEdicion() {
      return idEdicion;
    }

    public void setIdEdicion(Long idEdicion) {
      this.idEdicion = idEdicion;
    }

    public Integer getAnoPublicacion() {
      return anoPublicacion;
    }

    public void setAnoPublicacion(Integer anoPublicacion) {
      this.anoPublicacion = anoPublicacion;
    }

    public String getImagenPortada() {
      return imagenPortada;
    }

    public void setImagenPortada(String imagenPortada) {
      this.imagenPortada = imagenPortada;
    }

    public String getPdfUrl() {
      return pdfUrl;
    }

    public void setPdfUrl(String pdfUrl) {
      this.pdfUrl = pdfUrl;
    }

    public List<EjemplarLoteResult> getEjemplares() {
      return ejemplares;
    }

    public void setEjemplares(List<EjemplarLoteResult> ejemplares) {
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

  // ── Resultado por ejemplar ────────────────────────────────────────────────

  public static class EjemplarLoteResult {
    private Long idEjemplar;
    private String codigoEjemplar;
    private String codigoTopografico;
    private String codigoTopograficoConcat;
    private String clasificacionDecimal;
    private String cutterAutor;
    private String cutterTitulo;
    private String estadoEjemplar;
    private Long bibliotecaId;
    private String nombreBiblioteca;
    private String observaciones;

    public Long getIdEjemplar() {
      return idEjemplar;
    }

    public void setIdEjemplar(Long idEjemplar) {
      this.idEjemplar = idEjemplar;
    }

    public String getCodigoTopografico() {
      return codigoTopografico;
    }

    public void setCodigoTopografico(String codigoTopografico) {
      this.codigoTopografico = codigoTopografico;
    }

    public String getEstadoEjemplar() {
      return estadoEjemplar;
    }

    public void setEstadoEjemplar(String estadoEjemplar) {
      this.estadoEjemplar = estadoEjemplar;
    }

    public Long getBibliotecaId() {
      return bibliotecaId;
    }

    public void setBibliotecaId(Long bibliotecaId) {
      this.bibliotecaId = bibliotecaId;
    }

    public String getNombreBiblioteca() {
      return nombreBiblioteca;
    }

    public void setNombreBiblioteca(String nombreBiblioteca) {
      this.nombreBiblioteca = nombreBiblioteca;
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

    public String getClasificacionDecimal() {
      return clasificacionDecimal;
    }

    public void setClasificacionDecimal(String clasificacionDecimal) {
      this.clasificacionDecimal = clasificacionDecimal;
    }

    public String getCutterAutor() {
      return cutterAutor;
    }

    public void setCutterAutor(String cutterAutor) {
      this.cutterAutor = cutterAutor;
    }

    public String getCutterTitulo() {
      return cutterTitulo;
    }

    public void setCutterTitulo(String cutterTitulo) {
      this.cutterTitulo = cutterTitulo;
    }

    public String getCodigoTopograficoConcat() {

      String decimal = clasificacionDecimal != null
          ? clasificacionDecimal.trim()
          : "";

      String autor = cutterAutor != null
          ? cutterAutor.trim()
          : "";

      String titulo = cutterTitulo != null
          ? cutterTitulo.trim()
          : "";

      return String.join(" ",
          decimal,
          autor,
          titulo).trim();
    }

    public void setCodigoTopograficoConcat(String codigoTopograficoConcat) {
      this.codigoTopograficoConcat = codigoTopograficoConcat;
    }
  }
}