package com.proyecto.fhce.library.dto.request.library;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.proyecto.fhce.library.enums.EstadoEjemplar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EjemplarRequest {
  // @NotBlank(message = "El código del ejemplar es obligatorio")
  @Size(max = 50)
  private String codigoEjemplar;

  @Size(max = 100)
  private String codigoTopografico;
  @Size(max = 20)
  private String clasificacionDecimal;

  @Size(max = 10)
  private String cutterAutor;

  @Size(max = 10)
  private String cutterTitulo;
  @Size(max = 200)
  private String ubicacionFisica;

  // ✅ Ahora apunta a una edición, no directamente al libro
  @NotNull(message = "La edición es obligatoria")
  private Long edicionId;

  @NotNull(message = "La biblioteca es obligatoria")
  private Long bibliotecaId;

  private EstadoEjemplar estadoEjemplar;

  private LocalDate fechaAdquisicion;

  private String observaciones;

  public Long getBibliotecaId() {
    return bibliotecaId;
  }

  public void setBibliotecaId(Long bibliotecaId) {
    this.bibliotecaId = bibliotecaId;
  }

  public EstadoEjemplar getEstadoEjemplar() {
    return estadoEjemplar;
  }

  public void setEstadoEjemplar(EstadoEjemplar estadoEjemplar) {
    this.estadoEjemplar = estadoEjemplar;
  }

  public LocalDate getFechaAdquisicion() {
    return fechaAdquisicion;
  }

  public void setFechaAdquisicion(LocalDate fechaAdquisicion) {
    this.fechaAdquisicion = fechaAdquisicion;
  }

  public String getObservaciones() {
    return observaciones;
  }

  public void setObservaciones(String observaciones) {
    this.observaciones = observaciones;
  }

  public String getCodigoEjemplar() {
    return codigoEjemplar;
  }

  public void setCodigoEjemplar(String codigoEjemplar) {
    this.codigoEjemplar = codigoEjemplar;
  }

  public String getCodigoTopografico() {
    return codigoTopografico;
  }

  public void setCodigoTopografico(String codigoTopografico) {
    this.codigoTopografico = codigoTopografico;
  }

  public String getUbicacionFisica() {
    return ubicacionFisica;
  }

  public void setUbicacionFisica(String ubicacionFisica) {
    this.ubicacionFisica = ubicacionFisica;
  }

  public Long getEdicionId() {
    return edicionId;
  }

  public void setEdicionId(Long edicionId) {
    this.edicionId = edicionId;
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
}
